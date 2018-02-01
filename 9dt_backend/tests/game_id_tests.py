import unittest

import requests


class GameIdTests(unittest.TestCase):
    def setUp(self):
        self.base_url = 'http://localhost:8080'
        game = {
            'players': ['player1', 'player2'],
            'columns': 4,
            'rows': 4
        }
        resp = requests.post(self.base_url + '/drop_token', json=game)
        self.assertEqual(200, resp.status_code)
        self.game_id = resp.json()['gameId']

    def test_player_out_of_order(self):
        move = {'column': 3}
        url_2 = '{}/drop_token/{}/player2'.format(self.base_url, self.game_id)
        resp = requests.post(url_2, json=move)
        self.assertEqual(409, resp.status_code)

        url_1 = '{}/drop_token/{}/player1'.format(self.base_url, self.game_id)
        resp = requests.post(url_1, json=move)
        self.assertEqual(200, resp.status_code)
        self.assertEqual('{}/moves/0'.format(self.game_id), resp.json()['move'])

    # TODO: wins in 4 different ways
    def test_player1_wins_get_status_4_in_column(self):
        for i in range(6):
            move = {'column': i % 2}
            player = 'player1' if i % 2 == 0 else 'player2'
            url = '{}/drop_token/{}/{}'.format(self.base_url, self.game_id, player)
            resp = requests.post(url, json=move)
            self.assertEqual(200, resp.status_code)
            self.assertEqual('{}/moves/{}'.format(self.game_id, i), resp.json()['move'])

            status_resp = requests.get('{}/drop_token/{}'.format(self.base_url, self.game_id))
            self.assertEqual(
                status_resp.json(),
                {'players': ['player1', 'player2'], 'state': 'IN_PROGRESS'}
            )
        winning_move = {'column': 0}
        player = 'player1'
        url = '{}/drop_token/{}/{}'.format(self.base_url, self.game_id, player)
        resp = requests.post(url, json=winning_move)
        self.assertEqual(200, resp.status_code)
        self.assertEqual('{}/moves/{}'.format(self.game_id, 6), resp.json()['move'])

        status_resp = requests.get('{}/drop_token/{}'.format(self.base_url, self.game_id))
        self.assertEqual(
            status_resp.json(),
            {'players': ['player1', 'player2'], 'state': 'DONE', 'winner': 'player1'}
        )

        move_resp = requests.get('{}/drop_token/{}/moves/{}'.format(self.base_url, self.game_id, 6))
        self.assertEqual(
            move_resp.json(),
            {'type': 'MOVE', 'player': 'player1', 'column': 0}
        )

        error_move = {'column': 1}
        player = 'player2'
        url = '{}/drop_token/{}/{}'.format(self.base_url, self.game_id, player)
        resp = requests.post(url, json=error_move)
        self.assertEqual(404, resp.status_code)

    def test_player2_quits(self):
        # Invalid Game
        resp = requests.delete('{}/drop_token/{}/{}'.format(self.base_url, 123, 'player1'))
        self.assertEqual(404, resp.status_code)

        # Invalid Player
        resp = requests.delete('{}/drop_token/{}/{}'.format(self.base_url, self.game_id, 'foo'))
        self.assertEqual(404, resp.status_code)

        # Success
        resp = requests.delete('{}/drop_token/{}/{}'.format(self.base_url, self.game_id, 'player1'))
        self.assertEqual(202, resp.status_code)
        status_resp = requests.get('{}/drop_token/{}'.format(self.base_url, self.game_id))
        self.assertEqual(
            status_resp.json(),
            {'players': ['player1', 'player2'], 'state': 'DONE', 'winner': 'player2'}
        )

        # Game is Done already
        resp = requests.delete('{}/drop_token/{}/{}'.format(self.base_url, self.game_id, 'player1'))
        self.assertEqual(410, resp.status_code)

    def test_draw_returns_winner_as_none(self):
        moves = [
            ('player1', {'column': 0}),
            ('player2', {'column': 1}),
            ('player1', {'column': 0}),
            ('player2', {'column': 1}),
            ('player1', {'column': 0}),
            ('player2', {'column': 1}),
            ('player1', {'column': 2}),
            ('player2', {'column': 3}),
            ('player1', {'column': 2}),
            ('player2', {'column': 3}),
            ('player1', {'column': 2}),
            ('player2', {'column': 3}),
            ('player1', {'column': 1}),
            ('player2', {'column': 0}),
            ('player1', {'column': 3}),
            ('player2', {'column': 2}),
        ]
        for i, m in enumerate(moves):
            player, move = m
            url = '{}/drop_token/{}/{}'.format(self.base_url, self.game_id, player)
            resp = requests.post(url, json=move)
            self.assertEqual(200, resp.status_code)
            self.assertEqual('{}/moves/{}'.format(self.game_id, i), resp.json()['move'])

            status_resp = requests.get('{}/drop_token/{}'.format(self.base_url, self.game_id))
            if i < 15:
                self.assertEqual(
                    status_resp.json(),
                    {'players': ['player1', 'player2'], 'state': 'IN_PROGRESS'}
                )
            else:
                self.assertEqual(
                    status_resp.json(),
                    {'players': ['player1', 'player2'], 'state': 'DONE', 'winner': None}
                )

    def test_get_sublist_of_moves(self):
        moves = [
            ('player1', {'column': 0}),
            ('player2', {'column': 1}),
            ('player1', {'column': 0}),
            ('player2', {'column': 1}),
            ('player1', {'column': 0}),
            ('player2', {'column': 1})
        ]
        for i, m in enumerate(moves):
            player, move = m
            url = '{}/drop_token/{}/{}'.format(self.base_url, self.game_id, player)
            resp = requests.post(url, json=move)
            self.assertEqual(200, resp.status_code)
            self.assertEqual('{}/moves/{}'.format(self.game_id, i), resp.json()['move'])

        resp = requests.delete('{}/drop_token/{}/player2'.format(self.base_url, self.game_id))
        self.assertEqual(202,  resp.status_code)

        # invalid requests return 400
        moves_resp = requests.get('{}/drop_token/{}/moves'.format(self.base_url, self.game_id), params={'start': -1})
        self.assertEqual(400, moves_resp.status_code)

        moves_resp = requests.get('{}/drop_token/{}/moves'.format(self.base_url, self.game_id), params={'until': -1})
        self.assertEqual(400, moves_resp.status_code)

        moves_resp = requests.get('{}/drop_token/{}/moves'.format(self.base_url, self.game_id), params={'start': 2, 'until': 1})
        self.assertEqual(400, moves_resp.status_code)

        # no params, get all
        moves_resp = requests.get('{}/drop_token/{}/moves'.format(self.base_url, self.game_id))
        self.assertEqual(200, moves_resp.status_code)
        expected = [{'type': 'MOVE', 'column': move[1]['column'], 'player': move[0]} for move in moves]
        expected.append({'type': 'QUIT', 'player': 'player2'})
        self.assertEqual({'moves': expected}, moves_resp.json())

        # partial params are filled
        moves_resp = requests.get('{}/drop_token/{}/moves'.format(self.base_url, self.game_id), params={'start': 5})
        self.assertEqual(200, moves_resp.status_code)
        self.assertEqual({'moves': expected[5:7]}, moves_resp.json())

        moves_resp = requests.get('{}/drop_token/{}/moves'.format(self.base_url, self.game_id), params={'until': 5})
        self.assertEqual(200, moves_resp.status_code)
        self.assertEqual({'moves': expected[0:5]}, moves_resp.json())

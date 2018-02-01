import unittest

import requests


class DropTokenTests(unittest.TestCase):
    def setUp(self):
        self.base_url = 'http://localhost:8080'

    def test_create_valid_game(self):
        game = {
            'players': ['player1', 'player2'],
            'columns': 4,
            'rows': 4
        }
        resp = requests.post(self.base_url + '/drop_token', json=game)
        self.assertEqual(200, resp.status_code)
        game_id = resp.json()['gameId']

        get_resp = requests.get(self.base_url + '/drop_token')
        self.assertEqual(1, len(get_resp.json()['games']))

        # player quits so active games return 0
        delete_resp = requests.delete('{}/drop_token/{}/{}'.format(self.base_url, game_id, 'player2'))
        self.assertEqual(202, delete_resp.status_code)

        get_resp = requests.get(self.base_url + '/drop_token')
        self.assertEqual(0, len(get_resp.json()['games']))

    def test_create_invalid_game_wrong_number_of_players(self):
        too_few_players_game = {
            'players': ['player1'],
            'columns': 4,
            'rows': 4
        }
        resp = requests.post(self.base_url + '/drop_token', json=too_few_players_game)
        # print(resp.text)
        self.assertEqual(400, resp.status_code)

        too_many_players = {
            'players': ['player1', 'player2', 'player3'],
            'columns': 4,
            'rows': 4
        }
        resp = requests.post(self.base_url + '/drop_token', json=too_many_players)
        # print(resp.text)
        self.assertEqual(400, resp.status_code)

    def test_create_invalid_game_wrong_number_of_columns_or_rows(self):
        wrong_columns = {
            'players': ['player1', 'player2'],
            'columns': 2,
            'rows': 4
        }
        resp = requests.post(self.base_url + '/drop_token', json=wrong_columns)
        # print(resp.text)
        self.assertEqual(400, resp.status_code)

        wrong_rows = {
            'players': ['player1', 'player2'],
            'columns': 4,
            'rows': 2
        }
        resp = requests.post(self.base_url + '/drop_token', json=wrong_rows)
        # print(resp.text)
        self.assertEqual(400, resp.status_code)


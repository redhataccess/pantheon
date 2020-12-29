import os

from flask import jsonify, request


class ApiError(Exception):
    def __init__(self, message, status_code, err_code=None, details=None):
        Exception.__init__(self, status_code, err_code, message, details)
        self.status_code = status_code
        self.err_code = err_code
        self.message = message
        self.details = details

    def __str__(self):
        return "status_code: {}, err_code: {}, message: '{}', details: '{}'".format(self.status_code, self.err_code,
                                                                                    self.message, self.details)


def on_json_load_error(e):
    """
    Handles JSON parse error
    :param e:
    :return:
    """
    if request.mimetype != 'application/json':
        raise ApiError("Wrong media type", 415, details="Expected 'application/json' encoded data")
    raise ApiError("Data could not be parsed", 400, details=str(e))


def create_success_response(message, status_code=200):
    response = jsonify({'code': status_code, 'message': message})
    response.status_code = status_code
    return response


def get_docs_path_for(file_name):
    """
    Get absolute path for api files.
    """
    file_path = os.path.join("../api-docs", file_name)
    return file_path


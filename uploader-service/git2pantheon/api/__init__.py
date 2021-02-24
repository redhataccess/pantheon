from flask import Blueprint, jsonify
from flask_executor import Executor
from flask_cors import CORS
from git2pantheon.utils import ApiError

executor = Executor()
api_blueprint = Blueprint('api', __name__, url_prefix='/api/')
CORS(api_blueprint)


@api_blueprint.errorhandler(ApiError)
def error_handler(apiErr):
    error = {'code': apiErr.err_code if (apiErr.err_code is not None) else apiErr.status_code, 'message': apiErr.message,
             'details': apiErr.details if (apiErr.details is not None) else ""}

    response = jsonify({'error': error})
    response.status_code = apiErr.status_code
    return response

from uploader import pantheon

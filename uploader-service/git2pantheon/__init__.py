import logging
import os

from flask import Flask, json
from werkzeug.exceptions import HTTPException
from flasgger import Swagger
from .api.upload import api_blueprint, executor
import atexit


def create_app():
    app = Flask(__name__, instance_relative_config=True)
    app.config.from_mapping(
        EXECUTOR_TYPE="process",
        EXECUTOR_MAX_WORKERS="1",
        EXECUTOR_PROPAGATE_EXCEPTIONS=True
    )
    app.config.from_mapping(
        PANTHEON_SERVER=os.environ['PANTHEON_SERVER'],
        UPLOADER_PASSWORD=os.environ['UPLOADER_PASSWORD'],
        UPLOADER_USER=os.environ['UPLOADER_USER']
    )
    logging.basicConfig(level=logging.DEBUG)
    logging.getLogger('werkzeug').setLevel(logging.INFO)
    executor.init_app(app)
    app.register_blueprint(api_blueprint)
    app.config['SWAGGER'] = {
        'title': 'Git2Pantheon API',
        'description': 'API for Git2Pantheon, the uploader service',
        'uiversion': 2
    }
    Swagger(app)
    return app


def on_exit():
    print('on exit')
    executor.shutdown(wait=True)


atexit.register(on_exit)

app = create_app()


@app.errorhandler(HTTPException)
def handle_exception(e):
    """Return JSON instead of HTML for HTTP errors."""
    # start with the correct headers and status code from the error
    response = e.get_response()
    # replace the body with JSON
    response.data = json.dumps({"error": {
        "code": e.code,
        "message": e.name,
        "details": e.description,
    }})
    response.content_type = "application/json"
    return response

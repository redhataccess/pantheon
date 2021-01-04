import json
import logging
import os
import shutil
from collections import namedtuple
from os.path import expanduser

from flasgger import swag_from
from flask import (
    request,
    jsonify)
from marshmallow import ValidationError, INCLUDE

from uploader import pantheon
from . import api_blueprint
from . import executor
from .. import utils
from ..helpers import FileHelper, GitHelper, MessageHelper
from ..messaging import broker
from ..models.request_models import RepoSchema
from ..models.response_models import Status
from ..utils import ApiError, get_docs_path_for
from flask import current_app

logger = logging.getLogger(__name__)


@swag_from(get_docs_path_for('clone_api.yaml'))
@api_blueprint.route('/clone', methods=['POST'])
def push_repo():
    if not request.data:
        logger.error("The request did not contain data")
        raise ApiError(message="No data", status_code=400, details="The request did not contain data")
    request.on_json_loading_failed = utils.on_json_load_error
    data = request.get_json()

    repo = create_repo_object(data)

    parsed_url = GitHelper.parse_git_url(repo.repo)

    cloned_repo = clone_repo(parsed_url, repo)

    logger.info("starting upload of repo=" + repo.repo + " (local dir=" + cloned_repo.working_dir)
    pantheon.channel_name = parsed_url.repo
    executor.submit(upload_repo, cloned_repo)

    return jsonify({"status_key": parsed_url.repo}), 202


def clone_repo(parsed_url, repo):
    try:
        MessageHelper.publish(parsed_url.repo, json.dumps({"current_status": "Cloning repo " + repo.repo + ""}))
        logger.info("Cloning repo=" + repo.repo + " and branch=" + repo.branch)

        cloned_repo = GitHelper.clone(repo_url=repo.repo, branch=repo.branch,
                                      clone_dir=expanduser("~") + "/temp/" + FileHelper.get_random_name(10))
    except BaseException as e:
        logger.error("Cloning of repo=" + repo.repo + "  with branch=" + repo.branch + " failed due to error=" + str(e))
        # return jsonify({"error": "Error cloning " + repo.repo + " with branch  due to " + str(e)}), 500
        raise ApiError(message="Error in cloning " + parsed_url.repo, status_code=503,
                       details="Error cloning " + repo.repo + " with branch " + repo.branch + " due to " + str(e))
    return cloned_repo


def create_repo_object(data):
    try:
        repo_schema = RepoSchema(unknown=INCLUDE)
        repo = repo_schema.load(data)
    except ValidationError as error:
        logger.error("The data" + data + "was not valid due to " + str(error.messages))
        raise ApiError("Bad Request", 400, details="Repository URL cannot be empty")
    return repo


def upload_repo(cloned_repo):
    try:
        pantheon.start_process(numeric_level=10, pw=current_app.config['UPLOADER_PASSWORD'],
                               user=current_app.config['UPLOADER_USER'],
                               server=current_app.config['PANTHEON_SERVER'], directory=cloned_repo.working_dir,
                               use_broker=True)
    except Exception as e:
        logger.error("Upload failed due to error=" + str(e))

    logger.info("removing temporary cloned directory=" + cloned_repo.working_dir)
    shutil.rmtree(cloned_repo.working_dir)


@api_blueprint.route('/info', methods=['GET'])
def info():
    commit_hash = os.environ['COMMIT_HASH'] if not os.environ['COMMIT_HASH'] is None else ''
    return jsonify({'commit_hash': commit_hash}), 200


@swag_from(get_docs_path_for('status_api.yaml'))
@api_blueprint.route('/status', methods=['POST'])
def status():
    status_data = get_upload_data()

    status_message = Status(current_status=status_data['current_status'],
                            file_type=status_data.get('type_uploading', ""),
                            last_uploaded_file=status_data.get('last_uploaded_file'),
                            total_files_uploaded=status_data.get('total_files_uploaded'))
    return jsonify(
        dict(status=status_message.current_status,
             currently_uploading=status_message.processing_file_type,
             last_uploaded_file=status_message.last_uploaded_file)), 200


def get_upload_data():
    if not request.data:
        raise ApiError(message="No data", status_code=400, details="The request did not contain data")
    request.on_json_loading_failed = utils.on_json_load_error
    request_data = request.get_json()
    if not 'status_key' in request_data.keys():
        raise ApiError(message="Incorrect status key", status_code=400,
                       details="The request did not contain key named status_key ")

    status_data = json.loads(broker.get(request_data.get('status_key')))
    return status_data


@swag_from(get_docs_path_for('progress_update_api_all.yaml'))
@api_blueprint.route('/progress-update/all', methods=['POST'])
def progress_update():
    status_data = get_upload_data()
    # status_progress: UploadStatus = upload_status_from_dict(status_data)
    if status_data["server"] and status_data["server"]["response_code"] and not 200 <= int(status_data["server"][
                                                                                               "response_code"]) <= 400:
        return jsonify(
            dict(
                server_status=status_data["server"]["response_code"],
                server_message=status_data["server"]["response_details"]
            )
        )
    response_dict = dict(
        modules_uploaded=status_data['modules'],
        modules_not_uploaded=status_data['modules_not_processed'],
        assemblies_uploaded=status_data['assemblies'],
        assemblies_not_uploaded=status_data['assemblies_not_processed'],
        resources_uploaded=status_data['resources'],
        resources_not_uploaded=status_data['resources_not_processed'],
        module_variants_created=status_data['module_variants'],
        last_uploaded_file=status_data['last_file_uploaded'],
        total_files_uploaded=status_data['total_files_uploaded'],
        server_status="OK",
        server_message="Accepting requests",
        current_status=status_data['current_status'],
        file_type=status_data['type_uploading']
    )
    if status_data['other_status'] is not None:
        response_dict['extra_info'] = status_data['other_status']

    return jsonify(
        response_dict
    ), 200


@swag_from(get_docs_path_for('progress_update_api_modules.yaml'))
@api_blueprint.route('/progress-update/modules', methods=['POST'])
def progress_update_modules():
    status_data = get_upload_data()
    if status_data["server"] and status_data["server"]["response_code"] and not 200 <= int(status_data["server"][
                                                                                               "response_code"]) <= 400:
        return jsonify(
            dict(
                server_status=status_data["server"]["response_code"],
                server_message=status_data["server"]["response_details"]
            )
        )
    response_dict = dict(
        modules_uploaded=status_data['modules'],
        modules_not_uploaded=status_data['modules_not_processed'],
        last_uploaded_file=status_data['last_file_uploaded'],
        total_files_uploaded=len(status_data['modules']),
        server_status="OK",
        server_message="Accepting requests",
        current_status=status_data['current_status']
    )

    return response_dict, 200


@swag_from(get_docs_path_for('progress_update_api_assemblies.yaml'))
@api_blueprint.route('/progress-update/assemblies', methods=['POST'])
def progress_update_assemblies():
    status_data = get_upload_data()

    if status_data["server"] and status_data["server"]["response_code"] and not 200 <= int(status_data["server"][
                                                                                               "response_code"]) <= 400:
        return jsonify(
            dict(
                server_status=status_data["server"]["response_code"],
                server_message=status_data["server"]["response_details"]
            )
        )
    response_dict = dict(
        assemblies_uploaded=status_data['assemblies'],
        assemblies_not_uploaded=status_data['assemblies_not_processed'],
        last_uploaded_file=status_data['last_file_uploaded'],
        total_files_uploaded=len(status_data['assemblies']),
        server_status="OK",
        server_message="Accepting requests",
        current_status=status_data['current_status']
    )

    return jsonify(
        response_dict
    ), 200


@swag_from(get_docs_path_for('progress_update_api_resources.yaml'))
@api_blueprint.route('/progress-update/resources', methods=['POST'])
def progress_update_resources():
    status_data = get_upload_data()
    if status_data["server"] and status_data["server"]["response_code"] and not 200 <= int(status_data["server"][
                                                                                               "response_code"]) <= 400:
        return jsonify(
            dict(
                server_status=status_data["server"]["response_code"],
                server_message=status_data["server"]["response_details"]
            )
        )
    response_dict = dict(
        resources_uploaded=status_data['resources'],
        resources_not_uploaded=status_data['resources_not_processed'],
        last_uploaded_file=status_data['last_file_uploaded'],
        total_files_uploaded=len(status_data['resources']),
        server_status="OK",
        server_message="Accepting requests",
        current_status=status_data['current_status']
    )

    return jsonify(
        response_dict
    ), 200

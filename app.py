from flask import Flask, send_from_directory
import socket
import os.path
import random
import string
import os

app = Flask(__name__)


@app.route('/')
def the_app():
	"""Serve index.html treating is as string template"""
	with open('tmpl/index.html', encoding='utf-8') as tmpl:
		return tmpl.read().format(node=this_node, git_commit=git_commit_short)


@app.route('/assets/<path:path>')
def send_static(path):
	"""Serve static files of the presentation. For production this should go to ngix with proper CGI/uWSGI...."""
	return send_from_directory('tmpl/assets', path)



def random_string(letters: list, length=8):
    """Generate a random string of fixed length"""
    return ''.join(random.sample(letters, length))


# Initialize instance id - just a random hexstring. This makes it possible to observe load balancing to multiple instances
this_node = random_string(string.hexdigits, 4)
# inject commit id into web app
git_commit_short = os.environ['GHASH']

if __name__ == '__main__':
    app.run(debug=False, host='0.0.0.0')

from flask import Flask, send_from_directory
import socket
import os.path
app = Flask(__name__)


@app.route('/')
def the_app():
	#return app.send_static_file('tmpl/index.html')
	#path = 'tmpl\index.html'
	#return app.send_static_file(os.path.join('js', path).replace('\\','/')) 
	with open('tmpl/index.html', encoding='utf-8') as tmpl:
		return tmpl.read().format(node=this_node)


@app.route('/tmpl/<path:path>')
def send_static(path):
    return send_from_directory('tmpl', path)


this_node = socket.getfqdn()
if __name__ == '__main__':
    app.run(debug=False, host='0.0.0.0')

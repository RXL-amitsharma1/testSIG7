# PV Signal ROR_PRR Module 
     
## Running ROR_PRR flask app without docker:

1. To run code without a docker container, install Python if it is not pre-installed on the server or if it is in a version< 3.9
	- Once python has been successfully setup, check pip version by this command
	    * Command: **pip --version** </br>
	If pip version is old, then update it with the pop up command in the message

2. Create python virtual environment:
   * Command: **python -m venv *path/to/virtualenv*** </br>
   
3. Activate Virtual environment and install the dependencies
   * Command: **source *env_name*/bin/activate** </br>
   * Command: **python -m pip install -r requirements.txt** </br>
    
4. After successfully activating virtual environment execute python script for starting Flask application.
   * Command: **python ror_prr.py**</br>

## Running ROR_PRR Flask app with docker seperately from PVS application

1. To build docker container and save it  
    * Command: **docker build -t ror_prr -f Dockerfile**
    * Command **docker save -o ror_prr.tar.gz ror_prr**
2. Once docker archive is successfully built, move it to the desired server location ,to load image and start docker container
    * Command **docker load -i ror_prr.tar.gz**
    * Command **docker run -idt -p 6365:6365 --name ror_prr ror_prr**
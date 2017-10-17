#!/bin/bash

# Validación de vertificado
if [[ ! -f './private_key.pem' ]]; then
	echo "Necesita agregar una llave 'private_key.pem' en el directorio hosts"
	exit;
fi

# Iniciamos el playbook de ansible pasandole como argumento
# el inventario de hosts (en este caso configurado de AWS)
# y pasando el parametro de conexión paramiko debido al problema
# de los certificados .PEM de Amazon Web Services y pasando la
# lista de tareas de x-road para empezar las instalaciones
ansible-playbook -i hosts/deploy.txt xroad_init.yml -c paramiko
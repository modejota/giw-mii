import os, shutil, zipfile

"""
Project Gutenberg no permite descargar los ficheros .txt haciendo uso de un "scrapper propio".
La documentación nos indica que debemos acceder a robot/harvest como "último recurso" para descargar automáticamente.

Se ha usado la orden

wget -w 2 -m -H "http://www.gutenberg.org/robot/harvest?filetypes[]=txt&langs[]=en"

por terminal, parando la ejecución cuando se han descargado algo más de 450 ficheros comprimidos diferentes.
Por algún motivo, hay versiones dobles de algunos ficheros, que terminan en -8.txt, por lo que los eliminamos.
Al final, nos quedamos con algo más de 200 ficheros .txt en la carpeta proyecto/collection.
"""


# Obtener el directorio donde están los ficheros .zip descargados, debe ser el mismo que donde se ejecuta el script.
directorio = os.getcwd()

# La carpeta donde se moverán los ficheros .txt
destino = os.getcwd() + "/collection"

# Crear la carpeta destino si no existe
if not os.path.exists(destino):
    os.makedirs(destino)

# Recorrer el directorio y sus subdirectorios
for raiz, dirs, ficheros in os.walk(directorio):
    # Para cada fichero encontrado
    for fichero in ficheros:
        # Si es un fichero .zip
        if fichero.endswith(".zip"):
            # Obtener la ruta completa del fichero
            ruta_fichero = os.path.join(raiz, fichero)
            # Crear un objeto ZipFile para leer el contenido del fichero
            zip = zipfile.ZipFile(ruta_fichero)
            # Extraer todos los elementos del zip en el mismo directorio
            zip.extractall(raiz)
            # Cerrar el objeto ZipFile
            zip.close()
        # Si es un fichero .txt
        elif fichero.endswith(".txt"):
            # Obtener la ruta completa del fichero
            ruta_fichero = os.path.join(raiz, fichero)
            # Mover el fichero a la carpeta destino
            shutil.move(ruta_fichero, destino)

# Para cada fichero .txt en la carpeta destino
for fichero in os.listdir(destino):
    # Si el fichero termina en -8.txt. (Por algún motivo, hay copias de los ficheros con este nombre)
    if fichero.endswith("-8.txt"):
        # Eliminar el fichero
        os.remove(os.path.join(destino, fichero))

# No eliminamos los ficheros .zip descargados por si acaso. La descarga mediante el robot tarda bastante.

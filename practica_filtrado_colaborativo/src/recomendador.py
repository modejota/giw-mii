import os
import sys
from tkinter import Tk, filedialog
from configuration import Configuration
import numpy as np
import pandas as pd


class Recomendador:
    configuracion = Configuration()

    @staticmethod
    def pedir_fichero(title, initialdir=configuracion.DATA_DIR, filetypes=None):
        if filetypes is None:
            filetypes = [('Todos los ficheros', '.*')]
        root = Tk()
        root.withdraw()  # Ocultar ventana padre de Tk y mostrar la nuestra como popup
        root.attributes('-topmost', True)
        try:
            fichero = filedialog.askopenfilename(
                initialdir=initialdir, title=title,
                filetypes=filetypes)
            root.destroy()
        except (OSError, FileNotFoundError):
            print('No se ha podido abrir el fichero.')
            sys.exit(1)
        except Exception as e:
            print(f'Ha ocurrido un error inesperado: {e}')
            sys.exit(1)
        if len(fichero) == 0 or fichero is None:
            print('No se ha seleccionado ningún fichero.')
            sys.exit(1)

        return fichero

    def __init__(self):

        while True:
            default = input('¿Desea utilizar la configuración por defecto? (S/N): ')
            default.upper()
            if default == 'S' or default == 'N':
                break
            else:
                print('Opción no válida. Introduzca S o N.')
        if default == 'N':
            # No se comprueba que los ints sean realmente ints y que los strings sean realmente strings.
            # Se asume para no complicar demasiado la lógica del programa.
            while True:
                self.configuracion.MOVIES_TO_BE_RATED = int(input('Introduzca el número de películas a valorar: '))
                if self.configuracion.MOVIES_TO_BE_RATED <= 0:
                    print('El número de películas a valorar debe ser mayor que 0.')
                else:
                    break
            while True:
                self.configuracion.NEIGHBOURS = int(input('Introduzca el tamaño del vecindario: '))
                if self.configuracion.NEIGHBOURS <= 0:
                    print('El tamaño del vecindario debe ser mayor que 0.')
                else:
                    break
            while True:
                result = input('¿Desea limitar el número de recomendaciones? (S/N): ')
                result.upper()
                if result == 'S':
                    self.configuracion.USE_MAX_RESULTS = True
                    break
                elif result == 'N':
                    self.configuracion.USE_MAX_RESULTS = False
                    break
                else:
                    print('Opción no válida. Introduzca S o N.')
            if self.configuracion.USE_MAX_RESULTS:
                while True:
                    self.configuracion.MAX_RESULTS = int(input('Introduzca el número máximo de recomendaciones: '))
                    if self.configuracion.MAX_RESULTS <= 0:
                        print('El número máximo de recomendaciones debe ser mayor que 0.')
                    else:
                        break
            while True:
                result = input('¿Desea eliminar usuarios irrelevantes? (S/N): ')
                result.upper()
                if result == 'S':
                    self.configuracion.DELETE_IRRELEVANT_USERS = True
                    break
                elif result == 'N':
                    self.configuracion.DELETE_IRRELEVANT_USERS = False
                    break
                else:
                    print('Opción no válida. Introduzca S o N.')
            if self.configuracion.DELETE_IRRELEVANT_USERS:
                while True:
                    self.configuracion.THRESHOLD_TO_BE_IRRELEVANT = int(input('Introduzca el número mínimo de '
                                                                              'películas en común con nosotros para '
                                                                              'considerar al usuario relevante: '))
                    if self.configuracion.THRESHOLD_TO_BE_IRRELEVANT <= 0:
                        print('El umbral de relevancia debe ser mayor que 0.')
                    else:
                        break
            self.configuracion.MOVIES_FILE = self.pedir_fichero(
                "Seleccione el fichero con las base de datos de películas",
                filetypes=[('Ficheros de datos', '.item')])
            self.configuracion.RATINGS_FILE = self.pedir_fichero(
                "Seleccione el fichero con la base de datos de valoraciones",
                filetypes=[('Ficheros de datos', '.data')])
            print("\n")

        self.peliculas = pd.read_csv(
            filepath_or_buffer=self.configuracion.MOVIES_FILE,
            sep='|',
            header=None,
            engine='python',
            encoding='mbcs',  # ANSI encoding in Windows, el fichero no está en UTF-8
            usecols=[0, 1, 2],
            names=['idMovie', 'title', 'date'])

        self.valoraciones = pd.read_csv(
            filepath_or_buffer=self.configuracion.RATINGS_FILE,
            sep='\t',
            header=None,
            engine='python',
            encoding='mbcs',  # ANSI encoding in Windows, el fichero no está en UTF-8
            usecols=[0, 1, 2],
            names=['idUser', 'idMovie', 'rating'])

        self.num_peliculas_a_valorar = self.configuracion.MOVIES_TO_BE_RATED
        self.tamanio_vecindario = self.configuracion.NEIGHBOURS
        self.vecindario = {}
        self.valoraciones_por_usuario = {}
        self.recomendaciones = {}

    def pedir_valoraciones_al_usuario(self):
        peliculas_a_valorar = self.peliculas.sample(n=self.num_peliculas_a_valorar, replace=False)
        print('Por favor, valora las siguientes películas del 1 al 5:')

        contador = 1
        for _, row in peliculas_a_valorar.iterrows():
            titulo = row['title']
            while True:
                print(f'Película {contador} de {self.num_peliculas_a_valorar}: {titulo}')
                valoracion = input('Valoración: ')
                if valoracion == "" or int(valoracion) not in range(1, 6):
                    print('Valoración no válida. Estas deben estar entre 1 y 5 estrellas.')
                else:
                    break
            self.valoraciones_por_usuario[row['idMovie']] = int(valoracion)
            contador += 1

        print('Gracias por tu colaboración al valorar las películas. \n')

    def calcular_vecindario(self):
        print('Calculando vecindario...\n')

        usuarios = self.valoraciones['idUser'].unique()
        usuarios = list(np.insert(usuarios, 0, 0))  # Mi usuario es el 0, la DB empieza por 1.

        # Crear matriz que agrupe los usuarios y las películas
        matriz_valoraciones = pd.DataFrame(index=usuarios, columns=self.peliculas['idMovie'])
        matriz_valoraciones = matriz_valoraciones.fillna(0)

        # Rellenar la matriz con las valoraciones del usuario
        for movie in self.valoraciones_por_usuario.keys():
            matriz_valoraciones.iloc[0, movie - 1] = self.valoraciones_por_usuario[movie]
        # Añadir las valoraciones de los demás usuarios
        for usuario in usuarios[1:]:
            valoraciones_usuario = self.valoraciones[self.valoraciones['idUser'] == usuario]
            for _, row in valoraciones_usuario.iterrows():
                matriz_valoraciones.iloc[usuario, row['idMovie'] - 1] = row['rating']

        # Elimino a los usuarios con menos de X coincidencias, quedandome así con los de más de X coincidencias.
        if self.configuracion.DELETE_IRRELEVANT_USERS:
            usuarios_a_eliminar = list()
            grupos = self.valoraciones.groupby('idUser')['idMovie'].apply(list)
            for usuario, peliculas in grupos.items():
                common = set(self.valoraciones_por_usuario.keys()).intersection(set(peliculas))
                if len(common) < self.configuracion.THRESHOLD_TO_BE_IRRELEVANT:
                    usuarios_a_eliminar.append(usuario)
            matriz_valoraciones.drop(matriz_valoraciones[usuarios_a_eliminar], axis=0, inplace=True)

            # Obtener el numero real de filas, al eliminar usuarios puede ser menor que el tamaño del vecindario
            num_filas = matriz_valoraciones.shape[0]
            if num_filas < self.tamanio_vecindario:
                self.tamanio_vecindario = num_filas - 1

        correlation_matrix = np.corrcoef(matriz_valoraciones)
        usuarios_ordenados = np.argsort(correlation_matrix[0])[::-1]

        for i in range(1, self.tamanio_vecindario + 1):
            # No se chequea correlación menor que 0, podría disminuir tamaño del vecindario. Se deja al usuario decidir.
            self.vecindario[usuarios_ordenados[i]] = correlation_matrix[0][usuarios_ordenados[i]]

        if self.configuracion.SHOW_DEBUG_INFO:
            print('Vecindario (idUser : correlation):\n', self.vecindario)

    def calcular_recomendaciones(self):
        print('Calculando recomendaciones...\n')
        peliculas_valoradas = self.valoraciones_por_usuario.keys()
        valoraciones_medias = np.mean(list(self.valoraciones_por_usuario.values()))

        vecindario_media = {}
        for usuario in self.vecindario.keys():
            valoracion_vecindario = self.valoraciones[self.valoraciones['idUser'] == usuario]
            media_vecindario = np.mean(valoracion_vecindario['rating'])
            vecindario_media[usuario] = media_vecindario

        # Para cada pelicula no vista por el usuario, calcular el posible interés.
        for pelicula in self.peliculas['idMovie']:
            if pelicula not in peliculas_valoradas:
                similitud, corr_acum = 0, 0
                for usuario in self.vecindario.keys():
                    correlacion = self.vecindario[usuario]
                    valoracion = self.valoraciones[(self.valoraciones['idUser'] == usuario)
                                                   & (self.valoraciones['idMovie'] == pelicula)]['rating']
                    if len(valoracion) > 0:
                        similitud += correlacion * (valoracion.values[0] - vecindario_media[usuario])
                        corr_acum += abs(correlacion)
                interes = 0
                if corr_acum > 0:
                    interes = (similitud / corr_acum) + valoraciones_medias
                    interes = min(5, interes)  # El interés no puede ser mayor que 5 estrellas
                if interes >= 4:
                    self.recomendaciones[pelicula] = interes

        # Ordenar las recomendaciones ordenadas por interés descendente
        self.recomendaciones = dict(sorted(self.recomendaciones.items(), key=lambda item: item[1], reverse=True))

    def imprimir_recomendaciones(self):
        contador = 1
        for pelicula in self.recomendaciones.keys():
            if self.configuracion.USE_MAX_RESULTS and contador > self.configuracion.MAX_RESULTS:
                break
            titulo = self.peliculas[self.peliculas['idMovie'] == pelicula]['title'].values[0]
            print(f'{contador}. {titulo}. \tPuntuacion: {self.recomendaciones[pelicula]:.3f}')
            contador += 1

    def exportar_recomendaciones(self):
        contador = 1
        directorio = self.configuracion.RESULTS_DIR
        if not os.path.exists(directorio):
            os.makedirs(directorio)

        with open(self.configuracion.RESULTS_FILE, 'x') as f:
            for pelicula in self.recomendaciones.keys():
                titulo = self.peliculas[self.peliculas['idMovie'] == pelicula]['title'].values[0]
                f.write(f'{contador}. {titulo}. \tPuntuación: {self.recomendaciones[pelicula]:.3f}\n')
                contador += 1

    def ejecutar(self):
        self.pedir_valoraciones_al_usuario()
        self.calcular_vecindario()
        self.calcular_recomendaciones()
        while True:
            imprimir = input('¿Desea imprimir las recomendaciones? (S/N): ')
            imprimir.upper()
            if imprimir == 'S' or imprimir == 'N':
                break
            else:
                print('Opción no válida. Introduzca S o N.')
        if imprimir == 'S':
            self.imprimir_recomendaciones()
        while True:
            exportar = input('¿Desea exportar las recomendaciones? (S/N): ')
            exportar.upper()
            if exportar == 'S' or exportar == 'N':
                break
            else:
                print('Opción no válida. Introduzca S o N.')
        if exportar == 'S':
            try:
                self.exportar_recomendaciones()
                print('Recomendaciones exportadas correctamente.')
            except FileExistsError:
                print('Error al exportar las recomendaciones.')
        print('Fin del programa. Gracias por usar el sistema de recomendación.')

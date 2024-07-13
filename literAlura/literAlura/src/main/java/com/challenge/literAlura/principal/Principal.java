package com.challenge.literAlura.principal;

import com.challenge.literAlura.modelo.Autor;
import com.challenge.literAlura.modelo.DatosConsulta;
import com.challenge.literAlura.modelo.DatosLibro;
import com.challenge.literAlura.modelo.Libro;
import com.challenge.literAlura.repository.AutorRepository;
import com.challenge.literAlura.repository.LibroRepository;
import com.challenge.literAlura.service.ConsumoAPI;
import com.challenge.literAlura.service.ConvierteDatos;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Principal {
    private Scanner sc = new Scanner(System.in);
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();

    private AutorRepository autorRepository;
    private LibroRepository libroRepository;

    public Principal(AutorRepository autorRepository,LibroRepository libroRepository) {
        this.autorRepository = autorRepository;
        this.libroRepository = libroRepository;
    }

    public void menu(){
        int opcion = -1;
        var menu = """
                    ********************************
                    1. Buscar libro por titulo
                    2. Lista de todos los libros
                    3. Lista de autores
                    4. Lista de autores vivos en un determinado año
                    5. Listar libros por idioma
                    6. Listar top 10 Libros
                    0. Salir 
                    
                    ********************************
                    Ingrese una opcion para continuar
                    ********************************
                """;

        while(opcion != 0){
            System.out.println(menu);
            opcion = sc.nextInt();
            sc.nextLine();
            switch (opcion){
                case 1:
                    buscarLibroPorTitulo();
                    break;
                case 2:
                    listaTodosLosLibros();
                    break;
                case 3:
                    listaAutores();
                    break;
                case 4:
                    listaAutoresPorAnio();
                    break;
                case 5:
                    listaLibrosPorIdioma();
                    break;
                case 6:
                    listaTop10Libros();
                    break;
                case 0:
                    System.out.println("Hasta luego!");
                    break;
                default:
                    System.out.println("Ingrese una opcion valida");
                    opcion = sc.nextInt();
            }
        }

    }

    private DatosConsulta getDatos(String libroBuscado){
        var json = consumoAPI.consumoDatos("https://gutendex.com/books/?search=" + libroBuscado.toLowerCase().replace(" " , "+"));
        var datosAPI = conversor.convierteDatos(json, DatosConsulta.class);
        return datosAPI;
    }

    private void buscarLibroPorTitulo() {
        System.out.println("Ingrese el titulo del libro");
        String tituloBuscado = sc.nextLine();

        var datoLibro = getDatos(tituloBuscado);

        Optional<DatosLibro> libroAPI = datoLibro.resultados().stream()
                .filter(l -> l.titulo().toLowerCase().contains(tituloBuscado.toLowerCase()))
                .findFirst();

        Optional<Libro> librobdd = libroRepository.findByTituloContainsIgnoreCase(tituloBuscado);

        if(librobdd.isPresent()){
            System.out.println("El libro ya se encuentra en la Base de Datos");
        } else if (libroAPI.isPresent()) {
            List<Autor> autores = libroAPI.get().autor().stream()
                    .map(a -> autorRepository.findByNombreContainsIgnoreCase(a.nombreAutor())
                            .orElseGet(() -> autorRepository.save(new Autor(a))))
                    .collect(Collectors.toList());
            libroRepository.save(new Libro(libroAPI.get(), autores));
            System.out.println("El libro fue guardado con exito");
        } else {
            System.out.println("Libro no encontrado");
        }
    }

    private void listaTodosLosLibros() {
        List<Libro> libros = libroRepository.findAll();
        libros.forEach(System.out::println);
    }

    private void listaAutores() {
        List<Autor> autores = autorRepository.findAll();
        autores.forEach(System.out::println);
    }

    private void listaAutoresPorAnio() {
        System.out.println("Ingrese el año a consultar: ");
        Integer anio = sc.nextInt();
        sc.nextLine();

        List<Autor> autores = autorRepository.filtrarAutoresPorAnio(anio);
        autores.forEach(System.out::println);
    }

    private void listaLibrosPorIdioma() {
        System.out.println("Elija un idioma");
        System.out.println("""
                1. Espanol (es)
                2. Ingles (en)
                3. Portugues (pt)
                """);
        var opcion = sc.nextInt();
        String idioma = "";
        if(opcion == 1){
            idioma = "es";
        } else if (opcion == 2){
            idioma = "en";
        } else if (opcion == 3){
            idioma = "pt";
        } else {
            idioma = null;
            System.out.println("Opcion no valida");
        }

        List<Libro> librosPorIdioma = libroRepository.findByIdiomaContaining(idioma);
        if(librosPorIdioma.isEmpty()){
            System.out.println("No existen libros en " + idioma);
        } else {
            librosPorIdioma.forEach(System.out::println);
        }
    }

    private void listaTop10Libros() {
        List<Libro> top10Libros = libroRepository.findTop10ByOrderByNumeroDescargasDesc();
        System.out.println("Top 10 de libros: ");
        top10Libros.forEach(System.out::println);

    }

}

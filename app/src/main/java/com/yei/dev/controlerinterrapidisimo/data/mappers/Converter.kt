package com.yei.dev.controlerinterrapidisimo.data.mappers

/**
 * Interfaz genérica para convertir objetos de tipo [I] (Input) a tipo [O] (Output)
 *
 * @param I Tipo de entrada (generalmente DTO)
 * @param O Tipo de salida (generalmente entidad de dominio)
 */
interface Converter<I, O> {
    /**
     * Convierte un objeto de tipo [I] a tipo [O]
     *
     * @param input Objeto a convertir
     * @return Objeto convertido
     */
    fun convert(input: I): O

    /**
     * Convierte una lista de objetos de tipo [I] a una lista de tipo [O]
     *
     * @param input Lista de objetos a convertir
     * @return Lista de objetos convertidos
     */
    fun convertList(input: List<I>): List<O> =
        input.map { convert(it) }
}

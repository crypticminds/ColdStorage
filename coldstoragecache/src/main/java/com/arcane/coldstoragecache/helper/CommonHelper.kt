package com.arcane.coldstoragecache.helper

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

/**
 * A helper class for handling common operations.
 *
 * @author Anurag
 */
class CommonHelper {

    companion object {

        /**
         * An instance of object mapper.
         */
        private val objectMapper = jacksonObjectMapper()


        /**
         * Method that returns the object mapper.
         */
        fun getObjectMapper(): ObjectMapper {
            return objectMapper
        }
    }
}
package com.rahuldharmkar.offlinesynckit.sampleApp

object CustomerJsonSerializer {

    fun toJson(customer: Customer): String {
        return """
            {
              "id": "${customer.id}",
              "name": "${customer.name}",
              "phone": "${customer.phone}"
            }
        """.trimIndent()
    }
}
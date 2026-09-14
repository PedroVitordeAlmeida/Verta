package com.verta.backend.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Responsavel por abrir o pool de conexoes (HikariCP) e conectar o Exposed
 * ao banco Postgres que ja existe com as tabelas: empresas, usuarios,
 * templates, contratos, versoes_contrato, arquivos.
 *
 * As credenciais vem de variaveis de ambiente (ver .env.example), lidas
 * atraves do application.conf. Isso permite que quem for montar o
 * docker-compose apenas injete as env vars, sem precisar tocar no codigo.
 */
object DatabaseFactory {

    private lateinit var dataSource: HikariDataSource

    fun init(config: ApplicationConfig) {
        val host = config.property("database.host").getString()
        val port = config.property("database.port").getString()
        val name = config.property("database.name").getString()
        val user = config.property("database.user").getString()
        val password = config.property("database.password").getString()

        val jdbcUrl = "jdbc:postgresql://$host:$port/$name"

        val hikariConfig = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            this.username = user
            this.password = password
            this.driverClassName = "org.postgresql.Driver"
            this.maximumPoolSize = 10
            this.isAutoCommit = false
            this.transactionIsolation = "TRANSACTION_READ_COMMITTED"
            // Por padrao o HikariCP tenta abrir uma conexao JA na inicializacao e,
            // se falhar, derruba o processo inteiro. Com -1 aqui, o backend sobe
            // normalmente mesmo que o banco ainda nao esteja disponivel (util em
            // docker-compose, onde o Postgres pode demorar alguns segundos a mais
            // pra ficar pronto) - ele so tenta conectar de verdade quando alguma
            // rota precisar acessar o banco, e ai sim retorna erro so daquela
            // requisicao, sem matar o servidor inteiro.
            this.initializationFailTimeout = -1
            validate()
        }

        dataSource = HikariDataSource(hikariConfig)
        Database.connect(dataSource)
    }

    /** Executa um bloco de acesso a dados fora da thread principal do Ktor. */
    suspend fun <T> dbQuery(block: () -> T): T =
        withContext(Dispatchers.IO) {
            transaction { block() }
        }
}

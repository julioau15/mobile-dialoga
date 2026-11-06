package com.aulasandroid.mobiledialoga.data
/**
 * Rotas da API do backend-dialoga (prefixo /v1/dialoga).
 * Espelham 100% os arquivos de rotas do backend Node.js (Express).
 * Rotas com parâmetro são funções: ApiRotas.Tarefas.porId(3)
 */
object ApiRotas {
    // Com adb reverse tcp:8080 tcp:8080 ativado, localhost aponta diretamente para o backend no PC
    const val BASE_URL = "http://localhost:8080/v1/dialoga"

    object Auth {
        const val BASE = "/auth"
        const val LOGIN = "/auth/login"
        fun loginGoogle(idGoogle: String) = "/auth/login/google/$idGoogle"
        const val RECUPERAR_SENHA_SOLICITAR = "/auth/recuperar-senha/solicitar"
        const val RECUPERAR_SENHA_VALIDAR_CODIGO = "/auth/recuperar-senha/validar-codigo"
        const val RECUPERAR_SENHA_REDEFINIR = "/auth/recuperar-senha/redefinir"
    }

    object Pacientes {
        const val BASE = "/pacientes"
        const val ME = "/pacientes/me"
        const val ME_REGISTROS_HUMOR = "/pacientes/me/registros-humor"
        const val ME_TRATAMENTOS = "/pacientes/me/tratamentos"
        const val ME_PROFISSIONAIS = "/pacientes/me/profissionais"
        const val PRIMEIRO_ACESSO = "/pacientes/me/primeiro-acesso"
        fun porId(id: Int) = "/pacientes/$id"
        fun perfilProfissional(id: Int) = "/pacientes/$id/perfil-profissional"
        fun anotacoes(id: Int) = "/pacientes/$id/anotacoes"
        fun solicitacoesAcesso(id: Int) = "/pacientes/$id/solicitacoes-acesso"
        fun meProfissional(idProfissional: Int) = "/pacientes/me/profissionais/$idProfissional"
    }

    object Profissionais {
        const val BASE = "/profissionais"
        const val ME = "/profissionais/me"
        const val ME_PACIENTES = "/profissionais/me/pacientes"
        const val ME_DISPONIBILIDADES = "/profissionais/me/disponibilidades"
        fun porId(id: Int) = "/profissionais/$id"
        fun mePaciente(idPaciente: Int) = "/profissionais/me/pacientes/$idPaciente"
    }

    object Administradores {
        const val BASE = "/administradores"
        const val ME = "/administradores/me"
        fun porId(id: Int) = "/administradores/$id"
    }

    object AprovacoesProfissionais {
        const val BASE = "/aprovacoes-profissionais"
        fun porId(id: Int) = "/aprovacoes-profissionais/$id"
        fun aprovar(id: Int) = "/aprovacoes-profissionais/$id/aprovar"
        fun reprovar(id: Int) = "/aprovacoes-profissionais/$id/recusar"
    }

    object Usuarios {
        const val BASE = "/usuarios"
        fun porId(id: Int) = "/usuarios/$id"
    }

    object Anotacoes {
        const val BASE = "/anotacoes"
        fun porId(id: Int) = "/anotacoes/$id"
    }

    object Categorias {
        const val BASE = "/categorias"
        fun porId(id: Int) = "/categorias/$id"
    }

    object CategoriasCompartilhadas {
        const val BASE = "/categorias-compartilhadas"
        fun porId(id: Int) = "/categorias-compartilhadas/$id"
    }

    object Consultas {
        const val BASE = "/consultas"
        fun porId(id: Int) = "/consultas/$id"
    }

    object Disponibilidades {
        const val BASE = "/disponibilidades"
        fun porId(id: Int) = "/disponibilidades/$id"
    }

    object Especialidades {
        const val BASE = "/especialidades"
        fun porId(id: Int) = "/especialidades/$id"
    }

    object ProfissionaisEspecialidade {
        const val BASE = "/profissionais-especialidade"
        fun porId(id: Int) = "/profissionais-especialidade/$id"
    }

    object Tratamentos {
        const val BASE = "/tratamentos"
        fun porId(id: Int) = "/tratamentos/$id"
    }

    object HorariosTratamento {
        const val BASE = "/horarios-tratamento"
        fun porId(id: Int) = "/horarios-tratamento/$id"
    }

    object RegistrosDose {
        const val BASE = "/registros-dose"
        fun porId(id: Int) = "/registros-dose/$id"
    }

    object RegistrosHumor {
        const val BASE = "/registros-humor"
        fun porId(id: Int) = "/registros-humor/$id"
    }

    object Notificacoes {
        const val BASE = "/notificacoes"
        const val MARCAR_TODAS_LIDAS = "/notificacoes/marcar-todas-lidas"
        fun porId(id: Int) = "/notificacoes/$id"
        fun marcarLida(id: Int) = "/notificacoes/$id/marcar-lida"
    }

    object PreferenciasNotificacao {
        const val BASE = "/preferencias-notificacao"
        fun porId(id: Int) = "/preferencias-notificacao/$id"
    }

    object Tarefas {
        const val BASE = "/tarefas"
        fun porId(id: Int) = "/tarefas/$id"
        fun perguntas(id: Int) = "/tarefas/$id/perguntas"
        fun enviar(id: Int) = "/tarefas/$id/enviar"
    }

    object TarefasDestinatarios {
        const val BASE = "/tarefas-destinatarios"
        fun porId(id: Int) = "/tarefas-destinatarios/$id"
        fun respostas(id: Int) = "/tarefas-destinatarios/$id/respostas"
    }

    object Perguntas {
        const val BASE = "/perguntas"
        fun porId(id: Int) = "/perguntas/$id"
    }

    object OpcoesPergunta {
        const val BASE = "/opcoes-pergunta"
        fun porId(id: Int) = "/opcoes-pergunta/$id"
    }

    object Respostas {
        const val BASE = "/respostas"
        fun porId(id: Int) = "/respostas/$id"
    }

    object SolicitacoesAcesso {
        const val BASE = "/solicitacoes-acesso"
        fun porId(id: Int) = "/solicitacoes-acesso/$id"
        fun aprovar(id: Int) = "/solicitacoes-acesso/$id/aprovar"
        fun recusar(id: Int) = "/solicitacoes-acesso/$id/recusar"
    }

    object Vinculos {
        const val BASE = "/vinculos"
        fun porId(id: Int) = "/vinculos/$id"
        fun aceitar(id: Int) = "/vinculos/$id/aceitar"
        fun recusar(id: Int) = "/vinculos/$id/recusar"
    }
}

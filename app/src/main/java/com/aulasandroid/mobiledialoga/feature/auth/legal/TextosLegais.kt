package com.aulasandroid.mobiledialoga.feature.auth.legal

/** Qual documento legal está sendo exibido. */
enum class TipoDocumento { TERMOS_DE_USO, POLITICA_DE_PRIVACIDADE }

/** Pedaços de conteúdo de uma seção. */
sealed interface Bloco {
    data class Paragrafo(val texto: String) : Bloco
    data class Subtitulo(val texto: String) : Bloco
    data class Lista(val itens: List<String>) : Bloco
}

data class Secao(val titulo: String, val blocos: List<Bloco>)

data class DocumentoLegal(
    val titulo: String,
    val versao: String,
    val ultimaAtualizacao: String,
    val introducao: List<String>,
    val secoes: List<Secao>
)

fun documentoDe(tipo: TipoDocumento): DocumentoLegal = when (tipo) {
    TipoDocumento.TERMOS_DE_USO -> TermosDeUso
    TipoDocumento.POLITICA_DE_PRIVACIDADE -> PoliticaDePrivacidade
}

private fun p(t: String) = Bloco.Paragrafo(t)
private fun s(t: String) = Bloco.Subtitulo(t)
private fun l(vararg i: String) = Bloco.Lista(i.toList())

// TODO: preencher a data e os campos entre colchetes antes de publicar.
private const val DATA_ATUALIZACAO = "16/11/2026"

// ======================= TERMOS DE USO =======================
val TermosDeUso = DocumentoLegal(
    titulo = "Termos de Uso do Dialoga",
    versao = "1.0",
    ultimaAtualizacao = DATA_ATUALIZACAO,
    introducao = listOf(
        "Bem-vindo ao Dialoga.",
        "Estes Termos de Uso estabelecem as condições para utilização da plataforma Dialoga e se aplicam aos usuários cadastrados nos perfis de Paciente, Profissional e Administrador.",
        "Ao criar uma conta e utilizar a plataforma, o usuário declara ter tido acesso a estes Termos de Uso e à Política de Privacidade."
    ),
    secoes = listOf(
        Secao("1. Sobre o Dialoga", listOf(
            p("O Dialoga é uma plataforma digital desenvolvida para auxiliar no acompanhamento e organização de informações relacionadas ao bem-estar e à rotina emocional dos usuários."),
            p("Entre suas funcionalidades poderão estar:"),
            l(
                "registro do sentimento do dia;",
                "registro da energia;",
                "registro do humor;",
                "registro das horas dormidas;",
                "registro de acontecimentos;",
                "registro de pensamentos;",
                "acompanhamento por calendário;",
                "geração de gráficos e relatórios;",
                "vinculação entre paciente e profissional;",
                "disponibilização e preenchimento de atividades;",
                "organização de informações inseridas pelo próprio usuário."
            ),
            p("O Dialoga funciona como uma ferramenta tecnológica de apoio e acompanhamento."),
            p("A plataforma, por si só, não realiza diagnóstico médico ou psicológico e não substitui avaliação, acompanhamento ou atendimento realizado por profissional habilitado.")
        )),
        Secao("2. Perfis de usuário", listOf(
            p("O Dialoga possui três níveis principais de acesso."),
            s("2.1 Paciente"),
            p("O Paciente poderá utilizar as funcionalidades destinadas ao acompanhamento de seus próprios registros."),
            p("Entre suas permissões poderão estar:"),
            l(
                "cadastrar e atualizar informações pessoais;",
                "registrar informações relacionadas ao seu dia;",
                "acompanhar seu histórico;",
                "visualizar gráficos e relatórios;",
                "cadastrar acontecimentos e pensamentos;",
                "consultar suas atividades;",
                "pesquisar ou vincular-se a um profissional;",
                "autorizar acesso do profissional às informações disponibilizadas;",
                "solicitar alteração ou exclusão de informações, quando aplicável."
            ),
            p("O paciente é responsável pela veracidade das informações fornecidas.")
        )),
        Secao("3. Profissional", listOf(
            p("O perfil Profissional destina-se aos profissionais habilitados para utilização das funcionalidades correspondentes."),
            p("O profissional poderá:"),
            l(
                "manter seu cadastro profissional;",
                "receber solicitações de vínculo;",
                "acompanhar pacientes vinculados;",
                "visualizar informações disponibilizadas pelo paciente dentro das permissões estabelecidas;",
                "acompanhar relatórios;",
                "disponibilizar atividades quando essa funcionalidade estiver disponível;",
                "utilizar as informações da plataforma como apoio ao acompanhamento profissional."
            ),
            p("O profissional deverá manter atualizadas as informações relacionadas ao seu registro profissional."),
            p("O profissional é responsável por utilizar os dados acessados exclusivamente para finalidades compatíveis com o acompanhamento do paciente e deverá observar seus deveres legais, éticos e profissionais relacionados à confidencialidade e ao sigilo."),
            p("O vínculo dentro do Dialoga não transfere à plataforma a responsabilidade pelos atos profissionais praticados pelo usuário profissional.")
        )),
        Secao("4. Administrador", listOf(
            p("O Administrador é responsável pelo gerenciamento técnico e operacional do Dialoga."),
            p("Suas atividades poderão compreender:"),
            l(
                "gerenciamento de contas;",
                "gerenciamento de permissões;",
                "suporte técnico;",
                "manutenção da plataforma;",
                "análise de falhas;",
                "segurança do sistema;",
                "controle operacional;",
                "tratamento de solicitações relacionadas à plataforma."
            ),
            p("O perfil Administrador não deverá utilizar informações pessoais ou sensíveis para finalidades diferentes das necessárias para administração, segurança, suporte ou cumprimento de obrigação legal."),
            p("O acesso técnico a informações sensíveis deverá ser limitado sempre que tecnicamente possível.")
        )),
        Secao("5. Cadastro e segurança da conta", listOf(
            p("Cada usuário deverá fornecer informações verdadeiras e atualizadas."),
            p("As credenciais de acesso são pessoais e não deverão ser compartilhadas com terceiros."),
            p("O usuário é responsável por preservar a confidencialidade de sua senha e deverá comunicar à administração do Dialoga caso identifique uso não autorizado de sua conta."),
            p("A plataforma poderá implementar mecanismos adicionais de segurança sempre que necessário.")
        )),
        Secao("6. Vinculação entre paciente e profissional", listOf(
            p("A vinculação entre paciente e profissional deverá ocorrer por mecanismo disponibilizado pelo Dialoga."),
            p("O profissional não deverá possuir acesso automático aos dados de qualquer paciente simplesmente por possuir cadastro na plataforma."),
            p("O acesso deverá estar relacionado ao vínculo existente entre as contas e às permissões disponibilizadas pelo sistema."),
            p("O paciente poderá visualizar quais profissionais estão vinculados à sua conta."),
            p("O encerramento do vínculo poderá limitar acessos futuros do profissional, sem prejuízo da conservação de informações quando houver obrigação legal, regulatória ou profissional aplicável.")
        )),
        Secao("7. Dados registrados pelo paciente", listOf(
            p("O paciente poderá inserir voluntariamente informações relacionadas ao seu cotidiano e estado emocional."),
            p("Esses registros podem compreender informações de caráter pessoal e sensível."),
            p("O usuário deverá evitar inserir informações pessoais de terceiros sem necessidade ou autorização.")
        )),
        Secao("8. Uso adequado da plataforma", listOf(
            p("É proibida a utilização do Dialoga para:"),
            l(
                "obter acesso não autorizado a contas ou informações;",
                "utilizar identidade falsa;",
                "fornecer registro profissional falso;",
                "tentar romper mecanismos de segurança;",
                "copiar ou alterar informações pertencentes a outros usuários sem autorização;",
                "distribuir códigos maliciosos;",
                "utilizar dados de pacientes para finalidades não autorizadas;",
                "praticar qualquer atividade ilícita utilizando a plataforma."
            )
        )),
        Secao("9. Segurança e privacidade", listOf(
            p("O Dialoga adotará medidas técnicas e administrativas destinadas a reduzir riscos de acesso não autorizado, perda, alteração ou divulgação inadequada de dados."),
            p("Nenhum sistema informatizado pode garantir risco zero de incidentes de segurança."),
            p("Caso seja identificado incidente relevante envolvendo dados pessoais, serão adotadas as providências previstas na legislação aplicável.")
        )),
        Secao("10. Relatórios e gráficos", listOf(
            p("Os gráficos e relatórios disponibilizados pelo Dialoga serão gerados a partir das informações registradas pelo usuário."),
            p("Esses recursos possuem caráter informativo e de acompanhamento."),
            p("Resultados apresentados pelo sistema não devem ser interpretados isoladamente como diagnóstico clínico.")
        )),
        Secao("11. Situações de emergência", listOf(
            p("O Dialoga não é um serviço de emergência."),
            p("Em situações envolvendo risco imediato à vida ou à integridade física, o usuário deverá procurar os serviços de emergência e profissionais competentes."),
            p("O aplicativo não deverá ser utilizado como único recurso para atendimento de situações emergenciais.")
        )),
        Secao("12. Suspensão da conta", listOf(
            p("Uma conta poderá ser suspensa ou limitada quando houver:"),
            l(
                "violação destes Termos;",
                "tentativa de fraude;",
                "comprometimento da segurança;",
                "utilização ilícita da plataforma;",
                "determinação legal ou administrativa."
            ),
            p("Sempre que possível e juridicamente cabível, o usuário será informado.")
        )),
        Secao("13. Exclusão da conta", listOf(
            p("O usuário poderá solicitar exclusão de sua conta."),
            p("A exclusão poderá resultar na indisponibilidade permanente de determinadas informações."),
            p("Alguns dados poderão permanecer armazenados quando sua conservação for necessária para cumprimento de obrigações legais, regulatórias, exercício de direitos ou outras hipóteses permitidas pela legislação.")
        )),
        Secao("14. Propriedade intelectual", listOf(
            p("O nome Dialoga, sua identidade visual, código-fonte, interfaces, elementos gráficos e demais componentes desenvolvidos para a aplicação não poderão ser copiados, modificados ou distribuídos sem autorização de seus respectivos titulares, ressalvados componentes de terceiros utilizados de acordo com suas próprias licenças.")
        )),
        Secao("15. Alterações nos Termos", listOf(
            p("Estes Termos poderão ser atualizados em razão de:"),
            l(
                "alterações no funcionamento do sistema;",
                "criação ou retirada de funcionalidades;",
                "mudanças legais ou regulatórias;",
                "aperfeiçoamentos relacionados à segurança e privacidade."
            ),
            p("A versão e a data de atualização deverão permanecer disponíveis ao usuário."),
            p("Quando a alteração exigir nova manifestação do usuário, o Dialoga solicitará novo aceite.")
        )),
        Secao("16. Contato", listOf(
            p("Solicitações relacionadas ao uso da plataforma poderão ser encaminhadas para:"),
            p("Responsável pelo Dialoga: [NOME/EMPRESA/INSTITUIÇÃO]"),
            p("E-mail: [E-MAIL]"),
            p("Canal de suporte: [CANAL]")
        )),
        Secao("17. Disposições finais", listOf(
            p("A utilização do Dialoga está sujeita à legislação brasileira."),
            p("Questões relacionadas à proteção de dados pessoais serão tratadas conforme a legislação aplicável e a Política de Privacidade da plataforma.")
        ))
    )
)

// ======================= POLÍTICA DE PRIVACIDADE =======================
val PoliticaDePrivacidade = DocumentoLegal(
    titulo = "Política de Privacidade do Dialoga",
    versao = "1.0",
    ultimaAtualizacao = DATA_ATUALIZACAO,
    introducao = listOf(
        "A privacidade dos usuários é um princípio essencial para o Dialoga.",
        "Esta Política de Privacidade explica como dados pessoais são coletados, utilizados, armazenados, acessados e protegidos durante a utilização da plataforma."
    ),
    secoes = listOf(
        Secao("1. A quem esta política se aplica", listOf(
            p("Esta Política aplica-se aos usuários dos perfis:"),
            l("Paciente;", "Profissional;", "Administrador.")
        )),
        Secao("2. Dados do Paciente", listOf(
            p("Conforme as funcionalidades utilizadas, poderão ser tratados dados como:"),
            s("Dados cadastrais"),
            l(
                "nome;",
                "e-mail;",
                "telefone;",
                "data de nascimento;",
                "CPF, caso necessário;",
                "senha de acesso, protegida tecnicamente."
            ),
            s("Dados registrados pelo paciente"),
            p("O usuário poderá voluntariamente registrar:"),
            l(
                "sentimento do dia;",
                "nível de energia;",
                "humor;",
                "quantidade de horas dormidas;",
                "acontecimentos;",
                "pensamentos;",
                "informações inseridas em atividades;",
                "histórico de registros;",
                "outras informações voluntariamente fornecidas."
            ),
            p("Determinadas informações podem revelar aspectos relacionados à saúde ou ao estado emocional do usuário e, portanto, poderão constituir dados pessoais sensíveis.")
        )),
        Secao("3. Dados do Profissional", listOf(
            p("Poderão ser tratados:"),
            l(
                "nome;",
                "e-mail;",
                "telefone;",
                "CPF, caso necessário;",
                "profissão;",
                "especialidade;",
                "número de registro profissional;",
                "Conselho Regional relacionado ao registro;",
                "informações de perfil;",
                "vínculos estabelecidos com pacientes;",
                "informações necessárias à segurança e funcionamento da conta."
            )
        )),
        Secao("4. Dados do Administrador", listOf(
            p("Poderão ser tratados:"),
            l(
                "nome;",
                "e-mail;",
                "credenciais de acesso;",
                "nível de permissão;",
                "registros técnicos relacionados às operações administrativas."
            )
        )),
        Secao("5. Dados técnicos", listOf(
            p("Durante a utilização do Dialoga, poderão ser gerados automaticamente determinados registros técnicos, incluindo:"),
            l(
                "data e horário de acesso;",
                "endereço IP;",
                "identificadores técnicos;",
                "registros de autenticação;",
                "registros de erro;",
                "registros relacionados à segurança;",
                "operações relevantes realizadas dentro do sistema."
            ),
            p("Essas informações poderão ser utilizadas para segurança, prevenção de fraude, diagnóstico de erros, auditoria e melhoria da plataforma.")
        )),
        Secao("6. Finalidades do tratamento", listOf(
            p("Os dados poderão ser tratados para:"),
            l(
                "criar e administrar contas;",
                "autenticar usuários;",
                "disponibilizar as funcionalidades da plataforma;",
                "armazenar registros realizados pelo próprio usuário;",
                "gerar históricos, gráficos e relatórios;",
                "permitir vínculo entre paciente e profissional;",
                "disponibilizar ao profissional informações autorizadas;",
                "prestar suporte;",
                "garantir a segurança da plataforma;",
                "prevenir fraudes;",
                "corrigir erros;",
                "cumprir obrigações legais e regulatórias;",
                "possibilitar exercício de direitos."
            ),
            p("Os dados não deverão ser utilizados para finalidades incompatíveis com aquelas informadas ao usuário.")
        )),
        Secao("7. Tratamento de dados pessoais sensíveis", listOf(
            p("Informações relacionadas à saúde e ao estado emocional possuem proteção especial."),
            p("O Dialoga deverá limitar seu tratamento às informações necessárias às funcionalidades oferecidas e às finalidades previamente informadas."),
            p("Quando o tratamento depender de consentimento, este deverá ser obtido de forma específica e destacada."),
            p("Outras bases legais previstas na legislação poderão ser aplicáveis conforme a natureza do tratamento, incluindo situações relacionadas a cumprimento de obrigação legal, exercício de direitos, proteção da vida, segurança e tutela da saúde quando preenchidos os requisitos legais.")
        )),
        Secao("8. Compartilhamento entre paciente e profissional", listOf(
            p("Os registros do paciente não deverão ficar disponíveis indistintamente a todos os profissionais cadastrados."),
            p("O profissional somente poderá acessar informações relacionadas aos pacientes vinculados à sua conta e dentro das permissões estabelecidas pelo sistema."),
            p("O Dialoga deverá possibilitar ao paciente identificar os profissionais vinculados à sua conta."),
            p("Sempre que tecnicamente aplicável, a plataforma deverá utilizar mecanismos de autorização para controlar o compartilhamento.")
        )),
        Secao("9. Acesso administrativo", listOf(
            p("O perfil Administrador possui finalidade técnica e operacional."),
            p("A existência de privilégios administrativos não significa autorização para livre consulta aos registros pessoais dos pacientes."),
            p("O acesso a conteúdos sensíveis deverá ser tecnicamente restrito e ocorrer somente quando necessário para:"),
            l(
                "suporte solicitado pelo usuário;",
                "investigação de incidentes;",
                "segurança;",
                "cumprimento de obrigação legal;",
                "operação indispensável da plataforma."
            ),
            p("Sempre que tecnicamente possível, deverão ser utilizados registros de auditoria das operações administrativas relevantes.")
        )),
        Secao("10. Compartilhamento com terceiros", listOf(
            p("O Dialoga não deverá comercializar dados pessoais dos usuários."),
            p("Dados poderão ser compartilhados com fornecedores indispensáveis ao funcionamento da plataforma, quando necessário, como serviços de:"),
            l(
                "hospedagem;",
                "banco de dados;",
                "autenticação;",
                "armazenamento;",
                "envio de comunicações;",
                "segurança da informação."
            ),
            p("O compartilhamento deverá ser limitado ao necessário para execução da atividade correspondente."),
            p("Dados também poderão ser disponibilizados quando houver obrigação ou determinação legal válida.")
        )),
        Secao("11. Segurança dos dados", listOf(
            p("O Dialoga deverá implementar medidas técnicas e administrativas destinadas a proteger as informações."),
            p("Entre as medidas poderão ser utilizadas:"),
            l(
                "criptografia de comunicação;",
                "hash seguro de senhas;",
                "autenticação;",
                "controle de acesso;",
                "segregação de permissões;",
                "registros de auditoria;",
                "cópias de segurança;",
                "mecanismos de prevenção contra acessos indevidos;",
                "atualização de componentes de segurança;",
                "limitação de privilégios administrativos."
            )
        )),
        Secao("12. Armazenamento", listOf(
            p("Os dados deverão permanecer armazenados somente pelo período necessário às finalidades informadas ou enquanto houver fundamento jurídico para sua conservação."),
            p("Após o término do tratamento, as informações deverão ser eliminadas ou anonimizadas quando aplicável, ressalvadas as hipóteses legais de conservação.")
        )),
        Secao("13. Direitos do usuário", listOf(
            p("Nos termos da legislação aplicável, o titular poderá solicitar, conforme o caso:"),
            l(
                "confirmação da existência de tratamento;",
                "acesso aos seus dados;",
                "correção de informações incorretas ou desatualizadas;",
                "informações sobre compartilhamento;",
                "anonimização, bloqueio ou eliminação de dados tratados irregularmente;",
                "eliminação de dados tratados com fundamento no consentimento, observadas as exceções legais;",
                "informação sobre a possibilidade de não fornecer consentimento;",
                "revogação do consentimento;",
                "exercício de outros direitos previstos legalmente."
            ),
            p("As solicitações deverão passar por procedimento destinado a confirmar a identidade do solicitante e proteger os próprios dados contra solicitações fraudulentas.")
        )),
        Secao("14. Revogação do consentimento", listOf(
            p("Quando determinado tratamento depender de consentimento, o usuário poderá revogá-lo pelos meios disponibilizados pelo Dialoga."),
            p("A revogação não torna ilícitos os tratamentos realizados validamente antes de sua retirada e poderá resultar na indisponibilidade de funcionalidades que dependam necessariamente daquele tratamento.")
        )),
        Secao("15. Exclusão da conta", listOf(
            p("O usuário poderá solicitar exclusão de sua conta."),
            p("Quando legal e tecnicamente aplicável, os dados serão eliminados ou anonimizados."),
            p("Determinadas informações poderão permanecer conservadas quando necessário para cumprimento de obrigação legal ou regulatória, segurança, prevenção de fraude ou exercício regular de direitos.")
        )),
        Secao("16. Incidentes de segurança", listOf(
            p("Caso seja identificado incidente de segurança envolvendo dados pessoais, o Dialoga adotará medidas para avaliar, conter e reduzir seus possíveis impactos."),
            p("Quando houver obrigação legal, os titulares afetados e a autoridade competente serão comunicados na forma aplicável.")
        )),
        Secao("17. Crianças e adolescentes", listOf(
            p("Caso o Dialoga permita futuramente o cadastro ou tratamento de dados de crianças ou adolescentes, deverão ser implementados procedimentos específicos compatíveis com a legislação aplicável, incluindo mecanismos relacionados à participação dos responsáveis legais quando exigidos.")
        )),
        Secao("18. Alterações desta Política", listOf(
            p("Esta Política poderá ser atualizada por razões legais, técnicas ou operacionais."),
            p("A versão e a data da última atualização deverão permanecer disponíveis."),
            p("Alterações relevantes serão apresentadas aos usuários e, quando necessário, será solicitado novo consentimento ou manifestação.")
        )),
        Secao("19. Controlador e contato sobre dados pessoais", listOf(
            p("Controlador: [NOME DA EMPRESA/INSTITUIÇÃO RESPONSÁVEL PELO DIALOGA]"),
            p("CNPJ/CPF: [INFORMAR QUANDO APLICÁVEL]"),
            p("E-mail para privacidade: [E-MAIL]"),
            p("Encarregado/canal de privacidade: [INFORMAR QUANDO APLICÁVEL]"),
            p("Solicitações relacionadas à privacidade ou aos direitos dos titulares poderão ser encaminhadas ao canal informado acima.")
        )),
        Secao("20. Legislação aplicável", listOf(
            p("O tratamento de dados pessoais realizado pelo Dialoga deverá observar a legislação brasileira aplicável, especialmente a Lei nº 13.709/2018 – Lei Geral de Proteção de Dados Pessoais (LGPD).")
        ))
    )
)

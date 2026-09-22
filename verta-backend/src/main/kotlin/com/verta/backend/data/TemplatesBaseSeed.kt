package com.verta.backend.data

/** Um dos 5 templates base entregues a toda empresa que contrata um plano (ver TemplateRepository). */
data class TemplateBase(
    val nome: String,
    val descricao: String,
    val conteudo: String
)

/**
 * Os 5 templates base oferecidos em todos os planos (Basico e Plus). A diferenca entre planos
 * nao e o conteudo, e sim a flag [com.verta.backend.models.Templates.validadoJuridicamente]
 * aplicada a essas copias quando clonadas para a empresa (ver TemplateRepository.clonarTemplatesBase):
 * no Plano Basico entram como nao validados juridicamente, no Plano Plus como validados.
 *
 * O marcador "## " (usado no texto de referencia) foi convertido para negrito ("**...**"),
 * ja que o editor/renderizador de templates do frontend so reconhece um nivel de titulo
 * (linha comecando com "# ").
 */
object TemplatesBaseSeed {
    val TEMPLATES: List<TemplateBase> = listOf(
        TemplateBase(
            nome = "Contrato de Prestação de Serviços",
            descricao = "Modelo padrão para contratação de prestação de serviços entre CONTRATANTE e CONTRATADO.",
            conteudo = """
                # CONTRATO DE PRESTAÇÃO DE SERVIÇOS

                Pelo presente instrumento particular, de um lado:

                **CONTRATANTE:** {{nome_contratante}}, inscrito(a) no CPF/CNPJ sob nº {{cpf_cnpj_contratante}}, com endereço em {{endereco_contratante}}, {{cidade_contratante}}/{{estado_contratante}}, CEP {{cep_contratante}}.

                E, de outro lado:

                **CONTRATADO:** {{nome_contratado}}, inscrito(a) no CPF/CNPJ sob nº {{cpf_cnpj_contratado}}, com endereço em {{endereco_contratado}}, {{cidade_contratado}}/{{estado_contratado}}, CEP {{cep_contratado}}.

                As partes resolvem celebrar o presente Contrato de Prestação de Serviços, mediante as cláusulas seguintes:

                **CLÁUSULA 1ª – DO OBJETO**

                O CONTRATADO prestará ao CONTRATANTE os seguintes serviços:

                **Descrição dos serviços:** {{descricao_servico}}

                **CLÁUSULA 2ª – DO PRAZO**

                A prestação dos serviços terá início em {{data_inicio}} e término previsto para {{data_fim}}.

                **CLÁUSULA 3ª – DO VALOR E PAGAMENTO**

                Pela prestação dos serviços, o CONTRATANTE pagará ao CONTRATADO o valor total de **R$ {{valor_contrato}}**.

                O pagamento será realizado da seguinte forma: {{forma_pagamento}}.

                O vencimento ocorrerá em {{data_vencimento}}.

                **CLÁUSULA 4ª – DAS OBRIGAÇÕES DO CONTRATADO**

                São obrigações do CONTRATADO:

                a) Executar os serviços descritos neste contrato;
                b) Cumprir os prazos acordados;
                c) Manter comunicação adequada com o CONTRATANTE;
                d) Zelar pela qualidade dos serviços prestados.

                **CLÁUSULA 5ª – DAS OBRIGAÇÕES DO CONTRATANTE**

                São obrigações do CONTRATANTE:

                a) Fornecer as informações necessárias para execução dos serviços;
                b) Efetuar os pagamentos nos prazos estabelecidos;
                c) Disponibilizar os materiais necessários, quando aplicável.

                **CLÁUSULA 6ª – DA RESCISÃO**

                O presente contrato poderá ser rescindido mediante aviso prévio de {{dias_aviso_previo}} dias.

                Em caso de rescisão antecipada, será aplicada multa de {{percentual_multa}}% sobre {{base_calculo_multa}}.

                **CLÁUSULA 7ª – DO FORO**

                Fica eleito o foro da comarca de {{cidade_foro}}/{{estado_foro}} para dirimir eventuais questões decorrentes deste contrato.

                E, por estarem de acordo, as partes assinam o presente instrumento.

                {{cidade_assinatura}}, {{data_assinatura}}.

                **CONTRATANTE:**
                {{nome_contratante}}

                CPF/CNPJ: {{cpf_cnpj_contratante}}

                **CONTRATADO:**
                {{nome_contratado}}

                CPF/CNPJ: {{cpf_cnpj_contratado}}
            """.trimIndent()
        ),
        TemplateBase(
            nome = "Contrato de Compra e Venda",
            descricao = "Modelo padrão para compra e venda de bens entre VENDEDOR e COMPRADOR.",
            conteudo = """
                # CONTRATO DE COMPRA E VENDA

                Pelo presente instrumento, de um lado:

                **VENDEDOR:** {{nome_vendedor}}, CPF/CNPJ nº {{cpf_cnpj_vendedor}}, residente/sediado em {{endereco_vendedor}}, {{cidade_vendedor}}/{{estado_vendedor}}.

                E, de outro:

                **COMPRADOR:** {{nome_comprador}}, CPF/CNPJ nº {{cpf_cnpj_comprador}}, residente/sediado em {{endereco_comprador}}, {{cidade_comprador}}/{{estado_comprador}}.

                As partes celebram o presente Contrato de Compra e Venda.

                **CLÁUSULA 1ª – DO OBJETO**

                O VENDEDOR declara ser legítimo proprietário do seguinte bem:

                **Descrição:** {{descricao_bem}}
                **Marca:** {{marca_bem}}
                **Modelo:** {{modelo_bem}}
                **Número de série:** {{numero_serie}}
                **Estado de conservação:** {{estado_conservacao}}

                **CLÁUSULA 2ª – DO VALOR**

                O valor total da presente negociação é de **R$ {{valor_venda}}**.

                **CLÁUSULA 3ª – DA FORMA DE PAGAMENTO**

                O COMPRADOR realizará o pagamento da seguinte forma:

                {{forma_pagamento}}

                Data prevista para pagamento: {{data_pagamento}}.

                **CLÁUSULA 4ª – DA ENTREGA**

                A entrega do bem será realizada em {{data_entrega}}, no endereço {{local_entrega}}.

                **CLÁUSULA 5ª – DA RESPONSABILIDADE**

                Após a entrega do bem, a responsabilidade pela sua guarda e conservação passará ao COMPRADOR.

                **CLÁUSULA 6ª – DA DECLARAÇÃO DAS PARTES**

                O VENDEDOR declara que, até a data da entrega, o bem encontra-se livre de {{pendencias_debitos}}.

                **CLÁUSULA 7ª – DO FORO**

                Fica eleito o foro da comarca de {{cidade_foro}}/{{estado_foro}}.

                {{cidade_assinatura}}, {{data_assinatura}}.

                **VENDEDOR:**
                {{nome_vendedor}}

                CPF/CNPJ: {{cpf_cnpj_vendedor}}

                **COMPRADOR:**
                {{nome_comprador}}

                CPF/CNPJ: {{cpf_cnpj_comprador}}

                **TESTEMUNHA 1:**
                {{nome_testemunha_1}}
                CPF: {{cpf_testemunha_1}}

                **TESTEMUNHA 2:**
                {{nome_testemunha_2}}
                CPF: {{cpf_testemunha_2}}
            """.trimIndent()
        ),
        TemplateBase(
            nome = "Contrato de Locação Residencial",
            descricao = "Modelo padrão para locação de imóvel residencial entre LOCADOR e LOCATÁRIO.",
            conteudo = """
                # CONTRATO DE LOCAÇÃO RESIDENCIAL

                **LOCADOR:** {{nome_locador}}, CPF nº {{cpf_locador}}, residente em {{endereco_locador}}, {{cidade_locador}}/{{estado_locador}}.

                **LOCATÁRIO:** {{nome_locatario}}, CPF nº {{cpf_locatario}}, residente em {{endereco_atual_locatario}}, {{cidade_atual_locatario}}/{{estado_atual_locatario}}.

                As partes celebram o presente Contrato de Locação Residencial.

                **CLÁUSULA 1ª – DO IMÓVEL**

                O LOCADOR dá em locação ao LOCATÁRIO o imóvel situado à:

                {{endereco_imovel}}, {{numero_imovel}}, {{complemento_imovel}}, {{bairro_imovel}}, {{cidade_imovel}}/{{estado_imovel}}, CEP {{cep_imovel}}.

                **CLÁUSULA 2ª – DA FINALIDADE**

                O imóvel será utilizado exclusivamente para fins residenciais.

                **CLÁUSULA 3ª – DO PRAZO**

                A locação terá duração de {{prazo_locacao}}, iniciando-se em {{data_inicio}} e terminando em {{data_fim}}.

                **CLÁUSULA 4ª – DO ALUGUEL**

                O valor mensal do aluguel será de **R$ {{valor_aluguel}}**, com vencimento todo dia {{dia_vencimento}} de cada mês.

                **CLÁUSULA 5ª – DO REAJUSTE**

                O aluguel poderá ser reajustado conforme {{indice_reajuste}} e periodicidade de {{periodicidade_reajuste}}.

                **CLÁUSULA 6ª – DA GARANTIA**

                Como garantia da locação, será utilizada a modalidade:

                {{tipo_garantia}}

                Valor da garantia, quando aplicável: **R$ {{valor_garantia}}**.

                **CLÁUSULA 7ª – DAS DESPESAS**

                Ficarão sob responsabilidade do LOCATÁRIO:

                {{despesas_responsabilidade_locatario}}

                Ficarão sob responsabilidade do LOCADOR:

                {{despesas_responsabilidade_locador}}

                **CLÁUSULA 8ª – DA RESCISÃO**

                O contrato poderá ser rescindido nas hipóteses previstas neste instrumento e na legislação aplicável.

                A multa contratual, quando aplicável, será de {{multa_rescisao}}.

                **CLÁUSULA 9ª – DO FORO**

                Fica eleito o foro da comarca de {{cidade_foro}}/{{estado_foro}}.

                {{cidade_assinatura}}, {{data_assinatura}}.

                **LOCADOR:**
                {{nome_locador}}

                CPF: {{cpf_locador}}

                **LOCATÁRIO:**
                {{nome_locatario}}

                CPF: {{cpf_locatario}}

                **TESTEMUNHA 1:**
                {{nome_testemunha_1}}
                CPF: {{cpf_testemunha_1}}

                **TESTEMUNHA 2:**
                {{nome_testemunha_2}}
                CPF: {{cpf_testemunha_2}}
            """.trimIndent()
        ),
        TemplateBase(
            nome = "Contrato de Compra e Venda de Veículo",
            descricao = "Modelo padrão para compra e venda de veículo entre VENDEDOR e COMPRADOR.",
            conteudo = """
                # CONTRATO DE COMPRA E VENDA DE VEÍCULO

                **VENDEDOR:** {{nome_vendedor}}, CPF nº {{cpf_vendedor}}, residente em {{endereco_vendedor}}, {{cidade_vendedor}}/{{estado_vendedor}}.

                **COMPRADOR:** {{nome_comprador}}, CPF nº {{cpf_comprador}}, residente em {{endereco_comprador}}, {{cidade_comprador}}/{{estado_comprador}}.

                As partes celebram o presente Contrato de Compra e Venda de Veículo.

                **CLÁUSULA 1ª – DO VEÍCULO**

                O objeto deste contrato é o seguinte veículo:

                **Marca:** {{marca_veiculo}}
                **Modelo:** {{modelo_veiculo}}
                **Ano de fabricação:** {{ano_fabricacao}}
                **Ano modelo:** {{ano_modelo}}
                **Cor:** {{cor_veiculo}}
                **Placa:** {{placa_veiculo}}
                **RENAVAM:** {{renavam}}
                **Chassi:** {{chassi}}
                **Quilometragem:** {{quilometragem}}

                **CLÁUSULA 2ª – DO VALOR**

                O valor acordado para a venda é de **R$ {{valor_veiculo}}**.

                **CLÁUSULA 3ª – DO PAGAMENTO**

                O pagamento será realizado mediante:

                {{forma_pagamento}}

                Data prevista para quitação: {{data_quitacao}}.

                **CLÁUSULA 4ª – DA ENTREGA**

                A entrega do veículo ocorrerá em {{data_entrega}}, no local {{local_entrega}}.

                **CLÁUSULA 5ª – DOS DÉBITOS E MULTAS**

                Até a data da entrega, serão de responsabilidade do VENDEDOR os débitos e obrigações referentes ao veículo existentes até {{data_referencia}};

                Após essa data, serão de responsabilidade do COMPRADOR as obrigações decorrentes da utilização do veículo.

                **CLÁUSULA 6ª – DA TRANSFERÊNCIA**

                O COMPRADOR compromete-se a realizar a transferência de propriedade do veículo dentro do prazo legal aplicável.

                **CLÁUSULA 7ª – DO ESTADO DO VEÍCULO**

                O COMPRADOR declara ter vistoriado o veículo e estar ciente de seu estado de conservação.

                Observações:

                {{observacoes_veiculo}}

                **CLÁUSULA 8ª – DO FORO**

                Fica eleito o foro da comarca de {{cidade_foro}}/{{estado_foro}}.

                {{cidade_assinatura}}, {{data_assinatura}}.

                **VENDEDOR:**
                {{nome_vendedor}}

                CPF: {{cpf_vendedor}}

                **COMPRADOR:**
                {{nome_comprador}}

                CPF: {{cpf_comprador}}

                **TESTEMUNHA 1:**
                {{nome_testemunha_1}}
                CPF: {{cpf_testemunha_1}}

                **TESTEMUNHA 2:**
                {{nome_testemunha_2}}
                CPF: {{cpf_testemunha_2}}
            """.trimIndent()
        ),
        TemplateBase(
            nome = "Termo de Confidencialidade",
            descricao = "Modelo padrão de NDA entre PARTE DIVULGADORA e PARTE RECEPTORA.",
            conteudo = """
                # TERMO DE CONFIDENCIALIDADE

                Pelo presente instrumento:

                **PARTE DIVULGADORA:** {{nome_parte_divulgadora}}, CPF/CNPJ nº {{cpf_cnpj_divulgadora}}, com endereço em {{endereco_divulgadora}}.

                **PARTE RECEPTORA:** {{nome_parte_receptora}}, CPF/CNPJ nº {{cpf_cnpj_receptora}}, com endereço em {{endereco_receptora}}.

                As partes estabelecem o presente Termo de Confidencialidade.

                **CLÁUSULA 1ª – DO OBJETO**

                O presente termo tem por finalidade proteger informações confidenciais compartilhadas entre as partes em razão de:

                {{finalidade_compartilhamento}}

                **CLÁUSULA 2ª – DAS INFORMAÇÕES CONFIDENCIAIS**

                Serão consideradas confidenciais as informações relacionadas a:

                {{descricao_informacoes_confidenciais}}

                Incluem-se, quando aplicável, documentos, dados comerciais, informações financeiras, estratégias, projetos, códigos, informações de clientes e demais informações não públicas.

                **CLÁUSULA 3ª – DAS OBRIGAÇÕES**

                A PARTE RECEPTORA compromete-se a:

                a) Utilizar as informações exclusivamente para a finalidade estabelecida neste termo;

                b) Não divulgar as informações a terceiros sem autorização;

                c) Adotar medidas razoáveis para proteger as informações recebidas;

                d) Informar à PARTE DIVULGADORA caso tome conhecimento de acesso ou divulgação não autorizada.

                **CLÁUSULA 4ª – DAS EXCEÇÕES**

                Não serão consideradas confidenciais as informações que:

                a) Já eram de conhecimento público antes do compartilhamento;

                b) Tornarem-se públicas sem violação deste termo;

                c) Forem obtidas legalmente de terceiros;

                d) Precisarem ser divulgadas por determinação legal ou judicial.

                **CLÁUSULA 5ª – DO PRAZO**

                As obrigações de confidencialidade permanecerão vigentes pelo período de {{prazo_confidencialidade}} após {{evento_inicio_prazo}}.

                **CLÁUSULA 6ª – DA PENALIDADE**

                Em caso de violação das obrigações estabelecidas neste termo, poderá ser aplicada multa de **R$ {{valor_multa}}**, sem prejuízo de outras medidas cabíveis conforme a legislação aplicável.

                **CLÁUSULA 7ª – DO FORO**

                Fica eleito o foro da comarca de {{cidade_foro}}/{{estado_foro}}.

                {{cidade_assinatura}}, {{data_assinatura}}.

                **PARTE DIVULGADORA:**
                {{nome_parte_divulgadora}}

                CPF/CNPJ: {{cpf_cnpj_divulgadora}}

                **PARTE RECEPTORA:**
                {{nome_parte_receptora}}

                CPF/CNPJ: {{cpf_cnpj_receptora}}

                **TESTEMUNHA 1:**
                {{nome_testemunha_1}}
                CPF: {{cpf_testemunha_1}}

                **TESTEMUNHA 2:**
                {{nome_testemunha_2}}
                CPF: {{cpf_testemunha_2}}
            """.trimIndent()
        )
    )
}

# PetCare360 Java

Aplicação web MVC e API REST para cadastro de pets, monitoramento de coleiras e atendimento entre tutores e veterinários. Usa o mesmo esquema Oracle `TB_*` da entrega Sprint 04 de Banco de Dados.

## Tecnologias

Java 21, Spring Boot 4, Spring MVC, Thymeleaf, Spring Data JPA, Spring Security, Bean Validation, Oracle Database, Flyway, Spring Events, Micrometer/Actuator e Maven.

## Pré-requisitos

- Java 21 ou superior e acesso ao Oracle.
- Esquema da Sprint 04 já executado na conta Oracle usada pela aplicação. O arquivo de referência está no projeto de banco de dados: `PetCare360_Sprint4_Completo.sql`.
- Packages `PKG_PETCARE360` e `PKG_PETCARE360_CLINICO` válidos na mesma conta. A trigger de auditoria `TRG_AUDITORIA_SENSOR` é criada pelo script SQL.

## Executar

Na pasta do projeto Java, configure a conexão e inicie:

```bash
export ORACLE_DB_URL='jdbc:oracle:thin:@HOST:PORT/SERVICE'
export ORACLE_DB_USERNAME='SEU_USUARIO'
export ORACLE_DB_PASSWORD='SUA_SENHA'
FLYWAY_BASELINE_ON_MIGRATE=true ./mvnw spring-boot:run
```

Use `FLYWAY_BASELINE_ON_MIGRATE=true` **somente na primeira execução** na conta Oracle correta, depois de executar o script da Sprint 04. Para as próximas execuções, rode normalmente:

```bash
./mvnw spring-boot:run
```

Abra `http://localhost:8080/login`. No primeiro início, o Flyway cria `FLYWAY_SCHEMA_HISTORY`, registra a versão 1 como base e executa `V2__verificar_esquema_petcare360.sql`. Essa migração confere as 15 tabelas, as packages e a trigger; não recria os objetos. O Hibernate também valida as tabelas (`ddl-auto=validate`). Sem `FLYWAY_BASELINE_ON_MIGRATE`, uma conta sem histórico Flyway não é adotada automaticamente. Para uma conta Oracle vazia, execute primeiro o script Sprint 04; não use o script antigo `SJ_*`.

Novas mudanças de estrutura devem ser acrescentadas ao projeto Java como arquivos `V3__descricao.sql`, `V4__descricao.sql` etc. em `src/main/resources/db/migration/`. O Flyway as executará uma vez, em ordem, na mesma conta Oracle da aplicação. Não edite migrações que já foram aplicadas.

## Uso principal

1. Entre ou crie uma conta como tutor ou veterinário. O cadastro atribui o perfil com `PKG_PETCARE360_CLINICO.PRC_ATRIBUIR_PERFIL`.
2. Cadastre um pet, a coleira e a primeira leitura com latitude e longitude.
3. Vincule um veterinário ao pet. Esse fluxo chama `PKG_PETCARE360_CLINICO.PRC_VINCULAR_VETERINARIO`.
4. Consulte as leituras e os alertas; o tutor ou veterinário vinculado pode mudar o status de um alerta por `PKG_PETCARE360_CLINICO.PRC_ATUALIZAR_ALERTA`, que também grava o histórico.
5. Troque mensagens, solicite consultas e registre recomendações. Esses cadastros chamam, respectivamente, `PRC_ENVIAR_MENSAGEM`, `PRC_AGENDAR_CONSULTA` e `PRC_CRIAR_RECOMENDACAO` da mesma package. Recomendações são criadas por veterinários vinculados.
6. O veterinário responsável ou administrador confirma uma consulta solicitada e conclui uma consulta confirmada. Tutor, veterinário responsável ou administrador pode cancelá-la enquanto estiver solicitada ou confirmada. O cancelamento preserva o registro em `TB_APPOINTMENT` com status `CANCELED`.
7. Ao solicitar, remarcar ou confirmar uma consulta, a aplicação reserva o minuto escolhido para o veterinário e recusa outro agendamento ativo nesse mesmo minuto. Consultas canceladas ou concluídas não ocupam o horário.
8. As telas de consultas, mensagens e recomendações exibem 10 registros por página para tutores e veterinários; o administrador também tem paginação. O filtro de status das consultas é aplicado antes da paginação.
9. Em **Pets → Detalhes**, tutores e veterinários vinculados veem gráficos das 30 leituras mais recentes de temperatura e batimentos, com os alertas do período marcados. A tabela de leituras permanece visível. O administrador continua com a tabela e os demais detalhes do pet.

Os endpoints REST de criação de mensagem, consulta e recomendação retornam `201 Created` sem corpo. As procedures atuais não devolvem o ID gerado; consulte os endpoints `GET` ou atualize a página para ver o novo registro.

A API está documentada em `http://localhost:8080/swagger-ui/index.html`. Envie telemetria por `POST /api/iot/data`. As leituras gravadas em `TB_SENSOR_DATA` acionam a trigger de auditoria do banco.

O nível de atividade recebido como percentual é armazenado nas faixas `LOW` (0–33), `MEDIUM` (34–66) e `HIGH` (67–100), conforme o tipo da coluna Oracle. Ao consultar uma leitura, a API representa as faixas por 10, 50 ou 90; o percentual original não é recuperável.

## Saúde, idioma e testes

- `GET /actuator/health`: público.
- `GET /actuator/metrics`: exige usuário `ROLE_ADMIN`.
- A navegação, as telas MVC, as mensagens de validação e os erros gerais da API usam `messages*.properties` em português e inglês. Os botões PT/EN no cabeçalho, login e cadastro salvam a escolha por um ano no navegador. Sem escolha salva, o idioma segue o navegador, com português do Brasil como padrão.
- O envio de mensagens e a gravação de leituras publicam eventos internos após o commit. Contadores de mensagens, leituras e alertas ficam disponíveis nas métricas.
- O Micrometer registra traces com amostragem configurável por `TRACING_SAMPLE_RATE` (padrão `0.1`); os logs usam formato ECS.
- Em produção com HTTPS, configure `SESSION_COOKIE_SECURE=true`.

Execute os testes com um esquema Oracle separado, preparado com o mesmo script:

```bash
./mvnw test
```

O teste de inicialização precisa de `ORACLE_DB_URL`, `ORACLE_DB_USERNAME` e `ORACLE_DB_PASSWORD`. Ele também confere as versões 1 e 2 em `FLYWAY_SCHEMA_HISTORY`. Sem essas variáveis, ele é ignorado e os testes unitários continuam rodando. Na primeira execução em um esquema de teste ainda sem histórico Flyway, use `FLYWAY_BASELINE_ON_MIGRATE=true ./mvnw test` uma única vez. Não use a conta de produção para testes.

Para executar o teste das procedures contra um **esquema Oracle descartável**, com pet e veterinário já vinculados, configure as três variáveis de conexão acima e rode:

```bash
PETCARE_RUN_ORACLE_INTEGRATION=true ./mvnw test
```

Esse teste confere mensagem, consulta, recomendação e histórico do alerta nas tabelas `TB_*`. As gravações de teste são revertidas ao final da transação. Sem a variável `PETCARE_RUN_ORACLE_INTEGRATION`, ele é ignorado.

### Conferência integrada com Oracle

Em um esquema de teste já preparado, entre na aplicação com tutor e veterinário:

1. Cadastre um pet e vincule um veterinário. Confira `TB_PET`, `TB_DEVICE` e `TB_PET_VETERINARIAN`.
2. Envie uma mensagem e agende uma consulta como tutor. Confira `TB_PET_MESSAGE` e `TB_APPOINTMENT`.
3. Crie uma recomendação como veterinário vinculado. Confira `TB_CARE_RECOMMENDATION`.
4. Atualize um alerta de `OPEN` para `IN_PROGRESS` e depois para `RESOLVED`. Confira `TB_ALERT` e `TB_ALERT_HISTORY`.
5. Envie uma leitura pela API IoT. Confira `TB_SENSOR_DATA` e `TB_AUDITORIA_SENSOR`.
6. Tente enviar recomendação como administrador sem vínculo e alterar alerta de outro tutor; essas ações devem ser recusadas. Um veterinário vinculado pode editar e desativar o pet, mas outro veterinário não.
7. Solicite, confirme, conclua e cancele consultas pela interface. Confira os status em `TB_APPOINTMENT` e confirme que o cancelamento não remove linhas. Em caso de falha no formulário, a interface deve mostrar uma página de erro legível.

## Integrantes

| Integrante | RM |
| --- | ---: |
| Leonardo Zerbinatti de Sales | 562992 |
| Luis Guilherme Borges Silva | 566548 |
| Rafael de Freitas Moraes | 563210 |
| Rafael Pascotte Mercadante | 564928 |

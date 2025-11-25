🚀 Quarkus: O que é?
Quarkus é um framework Java otimizado para GraalVM e Open J9 que oferece inicialização rápida e baixo consumo de memória, tornando-o ideal para microsserviços, Kubernetes e arquiteturas Serverless. Se deseja mais detalhes, acesse: https://quarkus.io/

💻 Executando em Modo de Desenvolvimento
Para desenvolver com live coding (recarga instantânea de código), utilize o comando:

./mvnw quarkus:dev

Dev UI
O Quarkus inclui uma Dev UI para monitoramento e configuração, acessível apenas neste modo:

http://localhost:8080/q/dev/

📦 Empacotamento e Execução
Pacote Padrão (Non-Uber Jar)
O comando padrão de empacotamento cria um jar que não é um über-jar (jar completo). As dependências são separadas.

Empacotar: ./mvnw package

Resultado: O arquivo principal é quarkus-run.jar no diretório target/quarkus-app/.

As dependências estão em target/quarkus-app/lib/.

Executar: java -jar target/quarkus-app/quarkus-run.jar

Criando um Über-Jar (Jar Completo)
Se preferir um único arquivo jar com todas as dependências incluídas:

Empacotar: ./mvnw package -Dquarkus.package.jar.type=uber-jar

Executar: java -jar target/*-runner.jar

🔨 Executáveis Nativos
Crie um binário nativo para a máxima performance e menor consumo de recursos com o GraalVM:

Build Local
Requisito: GraalVM instalado.

./mvnw package -Dnative

Build em Container
Se você não tem o GraalVM, utilize um container (Docker, Podman) para a build:

./mvnw package -Dnative -Dquarkus.native.container-build=true

Execução
Após a build, o binário pode ser executado diretamente:

./target/code-with-quarkus-1.0.0-SNAPSHOT-runner

Mais informações sobre a construção de nativos: https://quarkus.io/guides/maven-tooling

📚 Guias (Extensions) Relacionados
REST (guide): Implementação de Jakarta REST baseada em Vert.x com processamento em tempo de build.

⚠️ Incompatível com quarkus-resteasy e extensões que dependem dele.

JDBC Driver - H2 (guide): Permite a conexão com o banco de dados H2 via JDBC.

REST Jackson (guide): Adiciona suporte à serialização e desserialização Jackson para os serviços REST.

⚠️ Esta extensão não é compatível com quarkus-resteasy.

Hibernate ORM with Panache (guide): Simplifica o uso do Hibernate ORM com os padrões Active Record ou Repository.

📝 Pontos de Partida no Código
Hibernate ORM
Crie sua primeira entidade JPA (POJO anotado).

Consulte o guia oficial para saber mais sobre persistência de dados.

REST
Inicie facilmente seus serviços Web REST usando anotações JAX-RS (Jakarta RESTful Web Services).

Consulte o guia oficial para detalhes sobre como expor seus recursos.
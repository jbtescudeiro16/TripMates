# TripMates
## Objetivos
No âmbito da Unidade curricular de Sensorização e Ambiente, inserida no perfil de Sistemas Inteligentes do 1º ano do Mestrado em Engenharia Informática do ano letivo 2023/2024, foi-nos proposto a construção de uma aplicação que utilizasse como base dados recolhidos por sensores.
<p></p>
Decidimos construir um projeto, cujo objetivo principal ́é a construção de uma aplicaçao móvel que permita
aos utilizadores registar despesas de viagem, calcular automaticamente os custos
com base nos dados do carro e no preço atual dos combustíveis, e atribuir uma
nota à viagem com base na qualidade da condução. O TripMates permite
integrar sensores físicos, como o acelerometro do dispositivo movel, e sensores
virtuais, como o acesso ao telefone durante a viagem, para avaliar a qualidade
da viagem de forma automática.

A nossa aplicação foi desenvolvida em Java+Kotlin , utilizando a ferramenta AndroidStudio. O armazenamento dos dados foi feito utilizando o Google Firebase.

## Sensores Utilizados

O TripMates utiliza um conjunto de sensores como :
  1. **Localização (Físico)**- O TripMates- utiliza o sensor de localização físico do dispositivo móvel para rastrear a rota da viagem realizada. Essa informação é crucial para calcular a distância percorrida, a duração da viagem e outras métricas relacionadas à viagem. O cálculo da distância percorrida é feito através da soma acumulada das distâncias entre as posições registadas sempre que o dispositivo deteta uma mudança de posição. Isso permite que a App acompanhe continuamente o movimento do utilizador e forneça informações precisas sobre a rota e a distância total percorrida.

  2. **Acelerómetro (Físico)**- A aplicação utiliza o acelerómetro físico do dispositivo para avaliar a qualidade
da condução. O acelerómetro deteta os movimentos do veículo, a cada 5 segundos, como acelerações e desacelerações e analisa esses dados para determinar a
suavidade da condução. Com base nos dados do acelerómetro, o TripMates classifica a qualidade da condução em três categorias:
     1. **Leve**: Movimentos leves, no intervalo de 18 a 22 unidades de aceleração.Isso pode incluir pequenas acelerações e desacelerações suaves.
     2. **Normal**: Movimentos normais, no intervalo de 22 a 25 unidades de aceleração. Isso pode incluir acelerações moderadas e desacelerações controladas.
     3. **Forte**: Movimentos fortes, com mais de 25 unidades de aceleração. Isso pode incluir acelerações bruscas, travagens repentinas ou curvas acentuadas.
      
  3. **Desbloqueio da Tela do dispositivo (Virtual)**- O TripMates utiliza o sensor virtual de desbloqueio da tela do dispositivo para determinar se o utilizador está ativamente envolvido com a App durante a viagem. Isso ajuda a identificar se o utilizador utiliza o dispositivo enquanto dirige, o que pode afetar a segurança da viagem.
  4. **Registo dos Pre ̧cos dos Combust ́ıveis (Virtual)**-A app utiliza um Web scraper para registar os preços atuais dos combustíveis,capturando os dados sobre o preço médio dos combustíveis presentes na tabela disponível em [Mais Gasolina - Preço dos combustíveis](https://www.maisgasolina.com/).



## Modo de Funcionamento
### Recolha dos Dados
A recolha de dados no TripMates é realizada principalmente através dos sensores do dispositivo móvel. Isso inclui o sensor de localização para rastrear a rota da viagem, o sensor de desbloqueio da tela e o acelerómetro para avaliar
a qualidade da condução. Os dados são continuamente capturados durante a
viagem para fornecer uma análise detalhada da experiência do utilizador.
Além disso, é importante destacar que  ́e possível registar todos os parâmetros
durante toda a viagem. Quando o utilizador inicia uma viagem, o serviço do
TripMates passa para um Foreground Service, garantindo que a aplicação permaneça em primeiro plano e tenha prioridade, mesmo quando o dispositivo
estiver a executar outras tarefas em segundo plano. Isso permite uma recolha
contínua e confiável de dados, garantindo uma análise precisa e completa da
viagem.

### Armazenamento dos dados

Criamos 3 coleções para o armazenamento dos dados:
  1. **fuel prices**:esta coleção mantém informações atualizadas sobre os preços
dos combustíveis em Portugal.
  2. **trips**:aqui são armazenados os detalhes de cada viagem realizada pelos utilizadores. 
  3. **users**: nesta coleção, são mantidos os perfis dos utilizadores.


## Fluxogramas

  De seguida esão apresentados dois diagramas. 
<p>Abaixo podemos observar o diagrama que mostra a aruitetura da aplicação, bem como os componentes essenciais à realização da mesma. </p>

<p align="center">
  <img src="https://github.com/jbtescudeiro16/TripMates/blob/main/pics/arquitetura.drawio.png" alt="Arquitetura TripMates" width="30%"> 
</p>

*** 

<p>Em baixo encontra-se a imagem que mostra o fluxo na aplicação, que um utilizador pode tomar, mediante as ações selecionadas.</p>
<p align="center">
<img src="https://github.com/jbtescudeiro16/TripMates/blob/main/pics/utilizacao_TripMates.drawio.png" alt="Utilizacao TripMates" width="30%">
</p>

## Layout da aplicação
  Nesta secção serão apresentados os layouts finais da aplicação. Os layouts foram construídos utilizando a ferramenta Canva, que facilitou muito o trabalho.

### Página Inicial
  Assim que o utilizador entra na páginal inicial, é-lhe apresentada a figura da imagem abaixo. O utilizador tem a possibilidade de fazer login com as suas credenciais armazenadas na base de dados, ou caso ainda não possua uma conta, tem a possibilidade de fazer um novo registo que está mencionado na figura da subseção **Registo**.

<p align="center">
    <img src="https://github.com/jbtescudeiro16/TripMates/blob/main/pics/FirstPage.jpg" alt="Página Inicial do TripMates" width="250">
</p>

***

### Registo
  Na página de registo é possível criar uma nova conta, inserindo os dados do nome de utilizador, e-mail, password e confirmação da password. Após estes dados serem inseridos, e quando o utilizador cria no botão de registo, e automaticamente criada uma nova entrada na base de dados com estes dados, e o utilziador fica válido.

<p align="center">
    <img src="https://github.com/jbtescudeiro16/TripMates/blob/main/pics/Registo.jpg" alt="Página de Registo" width="250">
</p>

***

### Página principal do utilizador
  Depois da autenticação de um utilziador acontecer na sua normalidade, é apresentada uma página em que são listadas as viagens de um utilizador, da mais recente para a mais antiga, com os respetivos dados. É também apresentado o nome do utilizador em cima, bem como um botão que, quando clicado, vai para a página apresentada em **Começar Viagem**.

<p align="center">
    <img src="https://github.com/jbtescudeiro16/TripMates/blob/main/pics/ProfilePaage.jpg" alt="Página do utilizador" width="270">
</p>

***

### Começar Viagem
Assim que um utilizador seleciona iniciar uma nova viagem, surge a página em que é possível escolher o número de viajantes entre 1 e 10, o combustível que o carro consome, bem como o consumo médio do veículo por 100 km. Caso um utilzador não pretenda preencher estes dados, os mesmos são completados com os da última viagem. 

<p align="center">
    <img src="https://github.com/jbtescudeiro16/TripMates/blob/main/pics/Select_Fields.jpg" alt="Página de início de viagem" width="250">
</p>

***

### Início

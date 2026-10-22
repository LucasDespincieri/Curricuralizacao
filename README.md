# BASE! — Plataforma de Comunhão e Leitura Comunitária

Projeto de extensão universitária voltado ao desenvolvimento de uma plataforma web para o projeto **BASE!**, conduzido pela liderança pastoral[cite: 1, 2, 4]. A aplicação integra a troca de mensagens e reflexões entre membros em um ambiente comunitário com uma biblioteca dedicada à publicação, gestão e leitura de livros e estudos[cite: 1, 2].

---

## 📌 Sobre o Projeto

O **BASE!** centraliza o convívio e a produção de conteúdo da igreja em um único ecossistema web, resolvendo duas necessidades centrais[cite: 1, 2]:
1. **Comunicação Comunitária**: Um feed central para membros compartilharem pedidos de oração, avisos e reflexões com suporte a tópicos de discussão[cite: 2, 4].
2. **Estudos e Literatura**: Uma área onde o pastor e membros autorizados publicam livros e estudos sem distrações, garantindo que o material possa ser lido sem comentários externos e editado pelo autor após a publicação[cite: 1, 2].

Por se tratar de um **MVP (Produto Mínimo Viável)** acadêmico, o foco do desenvolvimento está na consistência das regras de negócio, na estabilidade do banco de dados e em uma interface intuitiva[cite: 1, 2].

---

## ✨ Funcionalidades do MVP

### 1. Comunidade & Feed
* **Linha do Tempo em 3 Colunas**: Navegação lateral, feed centralizado e widgets contextuais[cite: 3, 4].
* **Publicações com Tags**: Criação de posts com categorização por tópicos (como `#Oração`, `#Aviso`, `#Estudo`)[cite: 4].
* **Engajamento e Respostas**: Reações aos posts e seção de comentários encadeados para aprofundamento das conversas[cite: 1, 4].
* **Filtro Pastoral**: Visualização segmentada entre todos os posts da comunidade e publicações oficiais do pastor[cite: 4].

### 2. Biblioteca e Modo Leitura
* **Catálogo de Livros**: Listagem de materiais e estudos organizados por autor[cite: 1, 2].
* **Leitura Focada**: Ambiente limpo e diagramado para leitura contínua, estruturado por seções e sem espaço para comentários de terceiros[cite: 1, 2].
* **Gestão de Obras (Autor)**: Módulo para criação e edição contínua dos livros publicados pelo próprio usuário[cite: 2].

### 3. Área do Membro
* **Minha Estante**: Espaço para armazenar e acompanhar livros salvos da biblioteca[cite: 1, 2, 4].
* **Perfil do Usuário**: Exibição dos dados do membro, histórico de posts, comentários e acervo de livros próprios[cite: 1, 2, 4].

---

## 🏛️ Estrutura do Banco de Dados

O modelo relacional foi desenhado para sustentar as interações sociais e a persistência de livros[cite: 1]:

* `Usuario`: Registro de credenciais, dados pessoais e vínculo com localização (`Cidade`, `UF`, `Pais`, `bairro`)[cite: 1].
* `Post` & `Comentarios`: Armazenamento das postagens do feed e suas respectivas respostas[cite: 1].
* `ReacaoPost` & `ReacaoComentario`: Registro das reações de engajamento dos membros[cite: 1].
* `Livros`: Cadastro dos títulos, quantidade de seções, datas e arquivos vinculados ao autor[cite: 1].
* `Biblioteca` & `LivrosBiblioteca`: Associação dos livros adicionados à estante pessoal de cada usuário[cite: 1].

---

## 🖥️ Layout e Interfaces

A arquitetura visual é dividida em blocos funcionais baseados no layout do X/Twitter[cite: 3, 4]:
* **Sidebar (Esquerda)**: Acesso rápido a Início, Biblioteca, Minha Estante, Perfil e botão de Nova Publicação[cite: 4].
* **Área Central**: Exibição dinâmica alternada entre o feed de posts, o catálogo da biblioteca, o leitor de livros ou a edição de perfil[cite: 2, 4].
* **Coluna Lateral (Direita)**: Quadro de avisos e destaques da comunidade para consultas rápidas[cite: 3, 4].

---

## 🚀 Instalação e Execução

### Processo de desenvolvimento em andamento...

## 👥 Equipe 
 * Diogo Cardoso Arantes
 * João Felipe Oliveira Condulucci
 * Lucas Miguel Despincieri Santos
 * Leonardo Poletto

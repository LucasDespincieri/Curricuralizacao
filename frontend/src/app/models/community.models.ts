export type Role = 'PASTOR' | 'MEMBRO';

export type ReacaoTipo = 'CURTIR' | 'AMEM';

export interface Localizacao {
  idCidade: number;
  cidade: string;
  uf: string; // CHAR(2)
  pais: string;
  bairro: string;
}

export interface Usuario {
  idUsuario: number;
  nome: string;
  username: string;
  email: string;
  nascimento: string; // ISO Date
  dia_cadastro: string; // ISO Date
  role: Role;
  localizacao: Localizacao;
}

export interface Reacao {
  idReacao: number;
  idUsuario: number;
  tipo: ReacaoTipo; // VARCHAR(8)
}

export interface Comentario {
  idComentarios: number;
  idUsuario: number;
  autorNome: string;
  autorRole: Role;
  idPost: number;
  data: string;
  conteudo: string; // VARCHAR(5000)
  reacoesCount: number;
}

export interface Post {
  idPost: number;
  idUsuario: number;
  autorNome: string;
  autorRole: Role;
  titulo?: string; // VARCHAR(20)
  data: string;
  conteudo: string; // VARCHAR(5000)
  imagemUrl?: string | null; // URL da imagem no Cloudinary (opcional)
  tags?: string[];
  reacoes: {
    curtir: number;
    amem: number;
    userReacted?: ReacaoTipo | null;
  };
  comentariosCount: number;
  saved?: boolean;
}

export interface Capitulo {
  id?: number;
  ordem: number;
  tituloSecao: string;
  texto: string;
}

export interface Livro {
  idLivros: number;
  idUsuario: number;
  autorNome: string;
  autorRole: Role;
  titulo: string; // VARCHAR(200)
  descricao?: string;
  tempoLeituraMin?: number;
  corCapa?: string;
  publicado?: boolean;
  docx?: string; // VARCHAR(50) - referência ou conteúdo estruturado
  secoes: number;
  conteudoSecoes?: Capitulo[];
  data_inicio: string;
}

export interface ItemBiblioteca {
  idBiblioteca: number;
  idUsuario: number;
  idLivros: number;
  livro: Livro;
  path_livro_customizado_pdf?: string;
}

export interface AvisoMural {
  id: number;
  titulo: string;
  descricao: string;
  data: string;
  autorNome: string;
}

export interface Grifo {
  id: number;
  idLivro: number;
  ordemCapitulo: number;
  idUsuario: number;
  trecho: string;
  anotacao?: string;
  criadoEm: string;
}

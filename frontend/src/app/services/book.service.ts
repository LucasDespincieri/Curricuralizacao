import { effect, Injectable, signal } from '@angular/core';
import { Capitulo, Grifo, ItemBiblioteca, Livro, Usuario } from '../models/community.models';

const BOOKS_KEY = 'base_books_v1';
const SHELF_KEY = 'base_shelf_v1';
const GRIFOS_KEY = 'base_grifos_v1';

function loadStorage<T>(key: string, fallback: T): T {
  try {
    const raw = localStorage.getItem(key);
    return raw ? (JSON.parse(raw) as T) : fallback;
  } catch {
    return fallback;
  }
}

@Injectable({ providedIn: 'root' })
export class BookService {
  readonly livros = signal<Livro[]>(loadStorage<Livro[]>(BOOKS_KEY, [
    {
      idLivros: 1,
      idUsuario: 10,
      autorNome: 'Pr. Lucas Mendes',
      autorRole: 'PASTOR',
      titulo: 'Caminhos de Graça',
      descricao:
        'Um estudo sobre a graça que transforma nossas escolhas, relacionamentos e a maneira de caminhar com Deus.',
      tempoLeituraMin: 18,
      corCapa: '#2A4065',
      publicado: true,
      secoes: 3,
      data_inicio: '2024-06-12',
      conteudoSecoes: [
        {
          ordem: 1,
          tituloSecao: 'A graça que encontra',
          texto:
            'Antes de sermos capazes de dar qualquer passo em direção à mudança, a graça já nos alcançou no meio da estrada. Não se trata de mérito acumulado, mas de um convite diário para descansar na fidelidade de Deus.\n\nQuando olhamos para as Escrituras, percebemos que Deus nunca esperou que estivéssemos prontos para nos chamar. Ele nos encontra exatamente onde estamos e, com paciência, nos conduz para onde precisamos ir.',
        },
        {
          ordem: 2,
          tituloSecao: 'Escolhas no cotidiano',
          texto:
            'A espiritualidade cristã não acontece apenas nos momentos solenes de culto; ela se revela na maneira como respondemos a uma mensagem difícil, na paciência no trânsito e na generosidade dentro de casa.\n\nViver pela graça é aprender a responder com misericórdia mesmo quando o ambiente ao redor convida à pressa e ao julgamento.',
        },
        {
          ordem: 3,
          tituloSecao: 'Caminhando em comunidade',
          texto:
            'Ninguém foi criado para caminhar sozinho. A igreja local é o espaço onde aprendemos a carregar os fardos uns dos outros, celebrar pequenas vitórias e perseverar juntos nos dias nublados.',
        },
      ],
    },
    {
      idLivros: 2,
      idUsuario: 12,
      autorNome: 'Mariana Lopes',
      autorRole: 'MEMBRO',
      titulo: 'Oração que sustenta',
      descricao:
        'Práticas simples para criar um espaço de silêncio, escuta e confiança no cotidiano.',
      tempoLeituraMin: 8,
      corCapa: '#9C7864',
      publicado: true,
      secoes: 2,
      data_inicio: '2024-06-08',
      conteudoSecoes: [
        {
          ordem: 1,
          tituloSecao: 'O silêncio antes das palavras',
          texto:
            'Muitas vezes chegamos à oração com uma lista pronta de urgências, mas esquecemos de parar por alguns instantes para lembrar diante de Quem estamos.\n\nCinco minutos de silêncio atento podem transformar a qualidade do nosso diálogo com Deus.',
        },
        {
          ordem: 2,
          tituloSecao: 'Orações curtas ao longo do dia',
          texto:
            'Orar sem cessar não significa isolar-se do mundo, mas manter o coração aberto a Deus entre uma tarefa e outra.',
        },
      ],
    },
    {
      idLivros: 3,
      idUsuario: 1,
      autorNome: 'Ana Martins',
      autorRole: 'MEMBRO',
      titulo: 'A mesa e o caminho',
      descricao:
        'Reflexões sobre hospitalidade cristã, comunhão ao redor da mesa e cuidado mútuo no bairro.',
      tempoLeituraMin: 12,
      corCapa: '#7E6A56',
      publicado: true,
      secoes: 2,
      data_inicio: '2024-05-27',
      conteudoSecoes: [
        {
          ordem: 1,
          tituloSecao: 'Portas abertas',
          texto:
            'Nos evangelhos, grande parte dos encontros mais marcantes de Jesus aconteceu ao redor de uma mesa. Compartilhar o pão é um gesto simples que derruba muros e cria pertencimento.',
        },
        {
          ordem: 2,
          tituloSecao: 'Servir sem perfeccionismo',
          texto:
            'Hospitalidade não é sobre ter a casa impecável, mas sobre oferecer um lugar seguro onde as pessoas possam ser ouvidas e acolhidas.',
        },
      ],
    },
    {
      idLivros: 4,
      idUsuario: 10,
      autorNome: 'Pr. Lucas Mendes',
      autorRole: 'PASTOR',
      titulo: 'Fundamentos da Fé',
      descricao:
        'Guia introdutório para novos membros e grupos de estudo sobre os pilares do discipulado.',
      tempoLeituraMin: 22,
      corCapa: '#5C7062',
      publicado: true,
      secoes: 2,
      data_inicio: '2024-05-15',
      conteudoSecoes: [
        {
          ordem: 1,
          tituloSecao: 'O que significa seguir a Cristo',
          texto:
            'Discipulado é uma jornada contínua de aprendizado, prática e transformação de caráter ao lado da comunidade.',
        },
        {
          ordem: 2,
          tituloSecao: 'Escritura, oração e serviço',
          texto:
            'Três hábitos simples que sustentam uma vida espiritual saudável e frutífera ao longo dos anos.',
        },
      ],
    },
  ]));

  readonly estante = signal<ItemBiblioteca[]>(
    loadStorage<ItemBiblioteca[]>(SHELF_KEY, [
      {
        idBiblioteca: 1,
        idUsuario: 1,
        idLivros: 1,
        livro: {
          idLivros: 1,
          idUsuario: 10,
          autorNome: 'Pr. Lucas Mendes',
          autorRole: 'PASTOR',
          titulo: 'Caminhos de Graça',
          descricao:
            'Um estudo sobre a graça que transforma nossas escolhas, relacionamentos e a maneira de caminhar com Deus.',
          tempoLeituraMin: 18,
          corCapa: '#2A4065',
          publicado: true,
          secoes: 3,
          data_inicio: '2024-06-12',
        },
      },
      {
        idBiblioteca: 2,
        idUsuario: 1,
        idLivros: 2,
        livro: {
          idLivros: 2,
          idUsuario: 12,
          autorNome: 'Mariana Lopes',
          autorRole: 'MEMBRO',
          titulo: 'Oração que sustenta',
          descricao:
            'Práticas simples para criar um espaço de silêncio, escuta e confiança no cotidiano.',
          tempoLeituraMin: 8,
          corCapa: '#9C7864',
          publicado: true,
          secoes: 2,
          data_inicio: '2024-06-08',
        },
      },
    ]),
  );

  readonly grifos = signal<Grifo[]>(loadStorage<Grifo[]>(GRIFOS_KEY, []));

  constructor() {
    effect(() => {
      try {
        localStorage.setItem(BOOKS_KEY, JSON.stringify(this.livros()));
        localStorage.setItem(SHELF_KEY, JSON.stringify(this.estante()));
        localStorage.setItem(GRIFOS_KEY, JSON.stringify(this.grifos()));
      } catch {
        // ignore storage quota errors
      }
    });
  }

  getGrifos(idLivro: number, ordemCapitulo: number, idUsuario: number): Grifo[] {
    return this.grifos().filter(
      (g) =>
        g.idLivro === idLivro &&
        g.ordemCapitulo === ordemCapitulo &&
        g.idUsuario === idUsuario,
    );
  }

  addGrifo(
    idLivro: number,
    ordemCapitulo: number,
    idUsuario: number,
    trecho: string,
    anotacao?: string,
  ): void {
    const novo: Grifo = {
      id: Date.now(),
      idLivro,
      ordemCapitulo,
      idUsuario,
      trecho: trecho.trim(),
      anotacao: anotacao?.trim() || undefined,
      criadoEm: new Date().toISOString(),
    };
    this.grifos.update((list) => [novo, ...list]);
  }

  removeGrifo(id: number): void {
    this.grifos.update((list) => list.filter((g) => g.id !== id));
  }

  getLivroById(id: number): Livro | undefined {
    return this.livros().find((l) => l.idLivros === id);
  }

  isSaved(livroId: number): boolean {
    return this.estante().some((item) => item.idLivros === livroId);
  }

  toggleEstante(livro: Livro, userId: number): void {
    const exists = this.isSaved(livro.idLivros);
    if (exists) {
      this.estante.update((items) => items.filter((i) => i.idLivros !== livro.idLivros));
    } else {
      const novoItem: ItemBiblioteca = {
        idBiblioteca: Date.now(),
        idUsuario: userId,
        idLivros: livro.idLivros,
        livro,
      };
      this.estante.update((items) => [novoItem, ...items]);
    }
  }

  saveLivro(
    user: Usuario,
    payload: {
      idLivros?: number;
      titulo: string;
      descricao?: string;
      conteudoSecoes: Capitulo[];
      publicado?: boolean;
    },
  ): Livro {
    const secoes = payload.conteudoSecoes.length;
    const palavras = payload.conteudoSecoes
      .map((s) => s.texto.split(/\s+/).filter(Boolean).length)
      .reduce((acc, n) => acc + n, 0);
    const tempoLeituraMin = Math.max(3, Math.ceil(palavras / 160));

    if (payload.idLivros) {
      let updated!: Livro;
      this.livros.update((list) =>
        list.map((l) => {
          if (l.idLivros !== payload.idLivros) return l;
          updated = {
            ...l,
            titulo: payload.titulo.trim().slice(0, 200),
            descricao: payload.descricao?.trim() || l.descricao,
            conteudoSecoes: payload.conteudoSecoes,
            secoes,
            tempoLeituraMin,
            publicado: payload.publicado ?? l.publicado,
          };
          return updated;
        }),
      );
      return updated;
    }

    const novo: Livro = {
      idLivros: Date.now(),
      idUsuario: user.idUsuario,
      autorNome: user.nome,
      autorRole: user.role,
      titulo: payload.titulo.trim().slice(0, 200),
      descricao: payload.descricao?.trim() || 'Estudo compartilhado com a comunidade.',
      tempoLeituraMin,
      corCapa: '#2A4065',
      publicado: payload.publicado ?? true,
      secoes,
      conteudoSecoes: payload.conteudoSecoes,
      data_inicio: new Date().toISOString().split('T')[0],
    };

    this.livros.update((list) => [novo, ...list]);
    return novo;
  }
}

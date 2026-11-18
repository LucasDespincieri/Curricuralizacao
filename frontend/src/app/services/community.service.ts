import { HttpClient } from '@angular/common/http';
import { effect, inject, Injectable, signal } from '@angular/core';
import { catchError, of } from 'rxjs';
import { AvisoMural, Comentario, Post, ReacaoTipo, Usuario } from '../models/community.models';

const POSTS_KEY = 'base_posts_v1';
const COMMENTS_KEY = 'base_comments_v1';

function loadFromStorage<T>(key: string, fallback: T): T {
  try {
    const raw = localStorage.getItem(key);
    return raw ? (JSON.parse(raw) as T) : fallback;
  } catch {
    return fallback;
  }
}

@Injectable({ providedIn: 'root' })
export class CommunityService {
  private readonly http = inject(HttpClient);
  readonly posts = signal<Post[]>(loadFromStorage<Post[]>(POSTS_KEY, [
    {
      idPost: 1,
      idUsuario: 10,
      autorNome: 'Pr. Lucas Mendes',
      autorRole: 'PASTOR',
      titulo: 'Esperança Viva',
      data: new Date(Date.now() - 32 * 60 * 1000).toISOString(),
      conteudo:
        '“A esperança não decepciona.”\n\nQue a nossa semana comece lembrando que Deus continua trabalhando, mesmo quando ainda não conseguimos ver o resultado. #PalavraDoPastor',
      tags: ['#PalavraDoPastor'],
      reacoes: { curtir: 24, amem: 18, userReacted: null },
      comentariosCount: 2,
      saved: false,
    },
    {
      idPost: 2,
      idUsuario: 11,
      autorNome: 'Rafael Souza',
      autorRole: 'MEMBRO',
      data: new Date(Date.now() - 60 * 60 * 1000).toISOString(),
      conteudo:
        'Bom dia, família! Peço oração pela minha mãe, Dona Célia. Ela fará um procedimento amanhã e estamos confiantes de que o Senhor estará à frente de tudo.\n#Oração',
      tags: ['#Oração'],
      reacoes: { curtir: 12, amem: 21, userReacted: 'AMEM' },
      comentariosCount: 1,
      saved: false,
    },
    {
      idPost: 3,
      idUsuario: 10,
      autorNome: 'Pr. Lucas Mendes',
      autorRole: 'PASTOR',
      titulo: 'Culto de Domingo',
      data: new Date(Date.now() - 3 * 60 * 60 * 1000).toISOString(),
      conteudo:
        'Domingo é dia de estarmos juntos na casa do Pai. Teremos nossa série de estudos sobre Romanos 8 às 10h e às 18h. Traga um amigo! #Aviso #Estudo',
      tags: ['#Aviso', '#Estudo'],
      reacoes: { curtir: 31, amem: 14, userReacted: null },
      comentariosCount: 0,
      saved: true,
    },
    {
      idPost: 4,
      idUsuario: 1,
      autorNome: 'Ana Martins',
      autorRole: 'MEMBRO',
      titulo: 'Gratidão',
      data: new Date(Date.now() - 26 * 60 * 60 * 1000).toISOString(),
      conteudo:
        'Muito grata pelo grupo de estudo de quarta-feira! A leitura do capítulo 2 de Caminhos de Graça abriu muitas conversas boas no nosso pequeno grupo. #Estudo',
      tags: ['#Estudo'],
      reacoes: { curtir: 9, amem: 7, userReacted: 'CURTIR' },
      comentariosCount: 0,
      saved: false,
    },
  ]));

  readonly comentarios = signal<Comentario[]>(loadFromStorage<Comentario[]>(COMMENTS_KEY, [
    {
      idComentarios: 1,
      idUsuario: 1,
      autorNome: 'Ana Martins',
      autorRole: 'MEMBRO',
      idPost: 1,
      data: new Date(Date.now() - 20 * 60 * 1000).toISOString(),
      conteudo: 'Amém, pastor! Palavra necessária para começar a semana com paz.',
      reacoesCount: 4,
    },
    {
      idComentarios: 2,
      idUsuario: 11,
      autorNome: 'Rafael Souza',
      autorRole: 'MEMBRO',
      idPost: 1,
      data: new Date(Date.now() - 12 * 60 * 1000).toISOString(),
      conteudo: 'Obrigado pela reflexão, Pr. Lucas!',
      reacoesCount: 2,
    },
    {
      idComentarios: 3,
      idUsuario: 10,
      autorNome: 'Pr. Lucas Mendes',
      autorRole: 'PASTOR',
      idPost: 2,
      data: new Date(Date.now() - 40 * 60 * 1000).toISOString(),
      conteudo: 'Estamos em oração pela Dona Célia e por toda a família, Rafael!',
      reacoesCount: 8,
    },
  ]));

  constructor() {
    effect(() => {
      try {
        localStorage.setItem(POSTS_KEY, JSON.stringify(this.posts()));
        localStorage.setItem(COMMENTS_KEY, JSON.stringify(this.comentarios()));
      } catch {
        // ignore storage quota errors
      }
    });

    this.loadPosts();
  }

  loadPosts(): void {
    this.http
      .get<Post[]>('/api/posts', { withCredentials: true })
      .pipe(catchError(() => of(null)))
      .subscribe((remotePosts) => {
        if (remotePosts && remotePosts.length > 0) {
          this.posts.set(remotePosts);
        }
      });
  }

  readonly avisos = signal<AvisoMural[]>([
    {
      id: 1,
      titulo: 'Culto de Celebração — Domingo',
      descricao: 'Horários às 10h e 18h no templo principal. Recepção e espaço infantil abertos.',
      data: 'Este domingo',
      autorNome: 'Pr. Lucas Mendes',
    },
    {
      id: 2,
      titulo: 'Grupo de Oração Semanal',
      descricao: 'Todas as quartas-feiras às 20h. Envie seus pedidos de oração no feed com #Oração.',
      data: 'Quarta, 20h',
      autorNome: 'Pr. Lucas Mendes',
    },
    {
      id: 3,
      titulo: 'Novo Devocional na Biblioteca',
      descricao: 'O livro "Caminhos de Graça" já está disponível completo na Biblioteca.',
      data: 'Fixado',
      autorNome: 'Pr. Lucas Mendes',
    },
  ]);

  createPost(user: Usuario, conteudo: string, titulo?: string, tags: string[] = []): void {
    const novo: Post = {
      idPost: Date.now(),
      idUsuario: user.idUsuario,
      autorNome: user.nome,
      autorRole: user.role,
      titulo: titulo?.trim() ? titulo.trim().slice(0, 20) : undefined,
      data: new Date().toISOString(),
      conteudo: conteudo.trim(),
      tags,
      reacoes: { curtir: 0, amem: 0, userReacted: null },
      comentariosCount: 0,
      saved: false,
    };
    this.posts.update((list) => [novo, ...list]);

    this.http
      .post<Post>(
        '/api/posts',
        { titulo: novo.titulo, conteudo: novo.conteudo },
        { withCredentials: true },
      )
      .pipe(catchError(() => of(null)))
      .subscribe((created) => {
        if (created) {
          this.posts.update((list) => list.map((p) => (p.idPost === novo.idPost ? created : p)));
        }
      });
  }

  toggleReaction(postId: number, tipo: ReacaoTipo): void {
    this.posts.update((list) =>
      list.map((post) => {
        if (post.idPost !== postId) return post;
        const prev = post.reacoes.userReacted;
        let curtir = post.reacoes.curtir;
        let amem = post.reacoes.amem;

        if (prev === 'CURTIR') curtir = Math.max(0, curtir - 1);
        if (prev === 'AMEM') amem = Math.max(0, amem - 1);

        const next: ReacaoTipo | null = prev === tipo ? null : tipo;
        if (next === 'CURTIR') curtir += 1;
        if (next === 'AMEM') amem += 1;

        return {
          ...post,
          reacoes: { curtir, amem, userReacted: next },
        };
      }),
    );

    this.http
      .post<Post>(`/api/posts/${postId}/reagir`, { tipo }, { withCredentials: true })
      .pipe(catchError(() => of(null)))
      .subscribe((updated) => {
        if (updated) {
          this.posts.update((list) => list.map((p) => (p.idPost === postId ? updated : p)));
        }
      });
  }

  toggleSavePost(postId: number): void {
    this.posts.update((list) =>
      list.map((p) => (p.idPost === postId ? { ...p, saved: !p.saved } : p)),
    );
  }

  deletePost(postId: number): void {
    this.posts.update((list) => list.filter((p) => p.idPost !== postId));
    this.comentarios.update((list) => list.filter((c) => c.idPost !== postId));

    this.http
      .delete(`/api/posts/${postId}`, { withCredentials: true })
      .pipe(catchError(() => of(null)))
      .subscribe();
  }

  getCommentsForPost(postId: number): Comentario[] {
    return this.comentarios().filter((c) => c.idPost === postId);
  }

  addComment(postId: number, user: Usuario, conteudo: string): void {
    const texto = conteudo.trim();
    if (!texto) return;

    const novoComentario: Comentario = {
      idComentarios: Date.now(),
      idUsuario: user.idUsuario,
      autorNome: user.nome,
      autorRole: user.role,
      idPost: postId,
      data: new Date().toISOString(),
      conteudo: texto,
      reacoesCount: 0,
    };

    this.comentarios.update((list) => [...list, novoComentario]);
    this.posts.update((list) =>
      list.map((p) =>
        p.idPost === postId ? { ...p, comentariosCount: p.comentariosCount + 1 } : p,
      ),
    );

    this.http
      .post<Comentario>(
        `/api/posts/${postId}/comentar`,
        { conteudo: texto },
        { withCredentials: true },
      )
      .pipe(catchError(() => of(null)))
      .subscribe((saved) => {
        if (saved) {
          this.comentarios.update((list) =>
            list.map((c) => (c.idComentarios === novoComentario.idComentarios ? saved : c)),
          );
        }
      });
  }
}

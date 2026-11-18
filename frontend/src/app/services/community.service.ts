import { HttpClient } from '@angular/common/http';
import { computed, effect, inject, Injectable, signal, untracked } from '@angular/core';
import { catchError, of } from 'rxjs';
import { AvisoMural, Comentario, Post, ReacaoTipo, Usuario } from '../models/community.models';
import { AuthService } from './auth.service';

// Só os IDs dos posts salvos ficam no navegador (o backend ainda não guarda "salvos").
const SAVED_KEY = 'base_saved_posts_v1';

function loadSavedIds(): number[] {
  try {
    const raw = localStorage.getItem(SAVED_KEY);
    const ids = raw ? (JSON.parse(raw) as unknown) : [];
    return Array.isArray(ids) ? ids.filter((id): id is number => typeof id === 'number') : [];
  } catch {
    return [];
  }
}

// Limpa os dados de exemplo que versões antigas do site deixaram guardados no navegador.
try {
  localStorage.removeItem('base_posts_v1');
  localStorage.removeItem('base_comments_v1');
} catch {
  // navegador sem localStorage: nada a limpar
}

@Injectable({ providedIn: 'root' })
export class CommunityService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);

  /** Posts vindos da API (GET /api/posts). */
  readonly posts = signal<Post[]>([]);
  /** true até a primeira resposta da API chegar (para mostrar "Carregando..."). */
  readonly carregandoPosts = signal(true);
  /** Comentários já carregados, de todos os posts abertos. */
  readonly comentarios = signal<Comentario[]>([]);
  private readonly savedIds = signal<number[]>(loadSavedIds());
  private readonly comentariosCarregados = new Set<number>();

  /** Mural de Avisos: os últimos posts do Pastor marcados com #Aviso. */
  readonly avisos = computed<AvisoMural[]>(() =>
    this.posts()
      .filter((p) => p.autorRole === 'PASTOR' && /#aviso\b/i.test(p.conteudo))
      .slice(0, 3)
      .map((p) => ({
        id: p.idPost,
        titulo: p.titulo || p.conteudo.split('\n')[0].replace(/#\S+/g, '').trim().slice(0, 60) || 'Aviso',
        descricao: p.conteudo.replace(/#\S+/g, '').trim(),
        data: formatarDataCurta(p.data),
        autorNome: p.autorNome,
      })),
  );

  constructor() {
    effect(() => {
      try {
        localStorage.setItem(SAVED_KEY, JSON.stringify(this.savedIds()));
      } catch {
        // ignore storage quota errors
      }
    });

    // Recarrega o feed quando o usuário entra ou sai (as reações "minhas" dependem do login).
    effect(() => {
      this.auth.currentUser()?.idUsuario;
      untracked(() => this.loadPosts());
    });
  }

  loadPosts(): void {
    this.http
      .get<Post[]>('/api/posts', { withCredentials: true })
      .pipe(catchError(() => of(null)))
      .subscribe((remotePosts) => {
        this.carregandoPosts.set(false);
        if (remotePosts) {
          const salvos = this.savedIds();
          this.posts.set(remotePosts.map((p) => ({ ...p, saved: salvos.includes(p.idPost) })));
          this.comentariosCarregados.clear();
          this.comentarios.set([]);
        }
      });
  }

  /** Busca os comentários de um post na API (uma vez por post, ao abrir a conversa). */
  loadComments(postId: number): void {
    if (this.comentariosCarregados.has(postId)) return;
    this.comentariosCarregados.add(postId);
    this.http
      .get<Comentario[]>(`/api/posts/${postId}/comentarios`, { withCredentials: true })
      .pipe(catchError(() => of(null)))
      .subscribe((lista) => {
        if (!lista) {
          this.comentariosCarregados.delete(postId);
          return;
        }
        this.comentarios.update((atual) => [
          ...atual.filter((c) => c.idPost !== postId),
          ...lista,
        ]);
      });
  }


  createPost(
    user: Usuario,
    conteudo: string,
    titulo?: string,
    tags: string[] = [],
    imagemUrl?: string | null,
  ): void {
    const novo: Post = {
      idPost: Date.now(),
      idUsuario: user.idUsuario,
      autorNome: user.nome,
      autorRole: user.role,
      titulo: titulo?.trim() ? titulo.trim().slice(0, 20) : undefined,
      data: new Date().toISOString(),
      conteudo: conteudo.trim(),
      imagemUrl: imagemUrl ?? null,
      tags,
      reacoes: { curtir: 0, amem: 0, userReacted: null },
      comentariosCount: 0,
      saved: false,
    };
    this.posts.update((list) => [novo, ...list]);

    this.http
      .post<Post>(
        '/api/posts',
        { titulo: novo.titulo, conteudo: novo.conteudo, imagemUrl: novo.imagemUrl },
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
    this.savedIds.update((ids) =>
      ids.includes(postId) ? ids.filter((id) => id !== postId) : [...ids, postId],
    );
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

function formatarDataCurta(iso: string): string {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return '';
  return d.toLocaleDateString('pt-BR', { day: '2-digit', month: 'short' });
}

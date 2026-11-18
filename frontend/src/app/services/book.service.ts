import { HttpClient } from '@angular/common/http';
import { effect, inject, Injectable, signal, untracked } from '@angular/core';
import { catchError, of } from 'rxjs';
import { Capitulo, Grifo, ItemBiblioteca, Livro, Usuario } from '../models/community.models';
import { AuthService } from './auth.service';

// Limpa os dados de exemplo que versões antigas do site deixaram guardados no navegador.
try {
  localStorage.removeItem('base_books_v1');
  localStorage.removeItem('base_shelf_v1');
  localStorage.removeItem('base_grifos_v1');
} catch {
  // navegador sem localStorage: nada a limpar
}

@Injectable({ providedIn: 'root' })
export class BookService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);

  /** Livros vindos da API (GET /api/livros). */
  readonly livros = signal<Livro[]>([]);
  /** true até a primeira resposta da API chegar. */
  readonly carregandoLivros = signal(true);
  /** Estante do usuário logado (GET /api/estante). */
  readonly estante = signal<ItemBiblioteca[]>([]);
  /** Grifos do usuário já carregados (por capítulo aberto no leitor). */
  readonly grifos = signal<Grifo[]>([]);

  constructor() {
    this.loadBooks();

    // Estante e grifos são do usuário: recarrega quando ele entra ou sai.
    effect(() => {
      const logado = !!this.auth.currentUser();
      untracked(() => {
        this.grifos.set([]);
        if (logado) {
          this.loadEstante();
        } else {
          this.estante.set([]);
        }
      });
    });
  }

  loadBooks(): void {
    this.http
      .get<Livro[]>('/api/livros', { withCredentials: true })
      .pipe(catchError(() => of(null)))
      .subscribe((remote) => {
        this.carregandoLivros.set(false);
        if (remote) {
          this.livros.set(remote);
        }
      });
  }

  loadEstante(): void {
    this.http
      .get<ItemBiblioteca[]>('/api/estante', { withCredentials: true })
      .pipe(catchError(() => of(null)))
      .subscribe((remote) => {
        if (remote) {
          this.estante.set(remote);
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

  /** Busca na API os grifos do usuário logado em um capítulo. */
  loadGrifos(idLivro: number, ordemCapitulo: number): void {
    if (!this.auth.currentUser()) return;
    this.http
      .get<Grifo[]>(`/api/livros/${idLivro}/capitulos/${ordemCapitulo}/grifos`, {
        withCredentials: true,
      })
      .pipe(catchError(() => of(null)))
      .subscribe((lista) => {
        if (!lista) return;
        this.grifos.update((atual) => [
          ...atual.filter((g) => !(g.idLivro === idLivro && g.ordemCapitulo === ordemCapitulo)),
          ...lista,
        ]);
      });
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

    this.http
      .post<Grifo>(
        `/api/livros/${idLivro}/capitulos/${ordemCapitulo}/grifos`,
        { trecho: novo.trecho, anotacao: novo.anotacao ?? '' },
        { withCredentials: true },
      )
      .pipe(catchError(() => of(null)))
      .subscribe((salvo) => {
        this.grifos.update((list) =>
          salvo ? list.map((g) => (g.id === novo.id ? salvo : g)) : list.filter((g) => g.id !== novo.id),
        );
      });
  }

  removeGrifo(id: number): void {
    this.grifos.update((list) => list.filter((g) => g.id !== id));
    this.http
      .delete(`/api/grifos/${id}`, { withCredentials: true })
      .pipe(catchError(() => of(null)))
      .subscribe();
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
      this.http
        .delete(`/api/estante/${livro.idLivros}`, { withCredentials: true })
        .pipe(catchError(() => of(null)))
        .subscribe();
    } else {
      const novoItem: ItemBiblioteca = {
        idBiblioteca: Date.now(),
        idUsuario: userId,
        idLivros: livro.idLivros,
        livro,
      };
      this.estante.update((items) => [novoItem, ...items]);
      this.http
        .post(`/api/estante/${livro.idLivros}`, {}, { withCredentials: true })
        .pipe(catchError(() => of(null)))
        .subscribe();
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
    this.http
      .post<Livro>(
        '/api/livros',
        { titulo: novo.titulo, conteudoSecoes: novo.conteudoSecoes },
        { withCredentials: true },
      )
      .pipe(catchError(() => of(null)))
      .subscribe((created) => {
        if (created) {
          this.livros.update((list) => list.map((l) => (l.idLivros === novo.idLivros ? created : l)));
        }
      });
    return novo;
  }
}

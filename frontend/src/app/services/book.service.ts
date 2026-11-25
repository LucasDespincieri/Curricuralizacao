import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { effect, inject, Injectable, signal, untracked } from '@angular/core';
import { catchError, firstValueFrom, of } from 'rxjs';
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

  /** Formulário de Filtro da Biblioteca: busca no banco pelo título ou pelo autor (GET /api/livros?busca=). */
  async buscarLivros(termo: string): Promise<Livro[]> {
    return firstValueFrom(
      this.http.get<Livro[]>(`/api/livros?busca=${encodeURIComponent(termo.trim())}`, { withCredentials: true }),
    );
  }

  /** Exclui o livro (só o autor ou o Pastor). Tira da lista, da estante e dos grifos em memória. */
  async excluirLivro(id: number): Promise<void> {
    try {
      await firstValueFrom(this.http.delete(`/api/livros/${id}`, { withCredentials: true }));
    } catch (err) {
      throw new Error(mensagemErroLivro(err));
    }
    this.livros.update((list) => list.filter((l) => l.idLivros !== id));
    this.estante.update((list) => list.filter((i) => i.idLivros !== id));
    this.grifos.update((list) => list.filter((g) => g.idLivro !== id));
  }

  /** Busca um livro na API (usado quando a lista ainda não carregou). */
  async buscarLivro(id: number): Promise<Livro | null> {
    const local = this.getLivroById(id);
    if (local) return local;
    try {
      return await firstValueFrom(this.http.get<Livro>(`/api/livros/${id}`, { withCredentials: true }));
    } catch {
      return null;
    }
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

  /**
   * Cria ou atualiza um livro, sempre esperando o backend responder.
   * Livro novo: devolve o ID real (o leitor abre por esse ID) e recarrega a estante,
   * onde o backend já coloca o livro do autor.
   */
  async saveLivro(
    user: Usuario,
    payload: {
      idLivros?: number;
      titulo: string;
      descricao?: string;
      conteudoSecoes: Capitulo[];
      publicado?: boolean;
    },
  ): Promise<Livro> {
    const corpo = {
      titulo: payload.titulo.trim().slice(0, 200),
      descricao: payload.descricao?.trim() ?? '',
      conteudoSecoes: payload.conteudoSecoes,
    };

    if (payload.idLivros) {
      // Edição: PUT /api/livros/{id}. O backend só aceita se quem edita for o autor.
      let atualizado: Livro;
      try {
        atualizado = await firstValueFrom(
          this.http.put<Livro>(`/api/livros/${payload.idLivros}`, corpo, { withCredentials: true }),
        );
      } catch (err) {
        throw new Error(mensagemErroLivro(err));
      }
      const livro: Livro = { ...atualizado, publicado: payload.publicado ?? atualizado.publicado };
      this.livros.update((list) => list.map((l) => (l.idLivros === livro.idLivros ? livro : l)));
      // Grifos de seções removidas somem no backend: limpa o cache para recarregar.
      this.grifos.update((list) => list.filter((g) => g.idLivro !== livro.idLivros));
      return livro;
    }

    let criado: Livro;
    try {
      criado = await firstValueFrom(
        this.http.post<Livro>('/api/livros', corpo, { withCredentials: true }),
      );
    } catch (err) {
      throw new Error(mensagemErroLivro(err));
    }

    const livro: Livro = { ...criado, publicado: payload.publicado ?? criado.publicado };
    this.livros.update((list) => [livro, ...list.filter((l) => l.idLivros !== livro.idLivros)]);
    this.loadEstante();
    return livro;
  }
}

function mensagemErroLivro(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 401) return 'Sua sessão expirou. Saia e entre de novo.';
    if (err.status === 403) {
      const msg = err.error && typeof err.error === 'object' ? err.error.error : null;
      return typeof msg === 'string' && msg ? msg : 'Só o autor pode editar este livro.';
    }
    if ([0, 502, 503, 504].includes(err.status)) {
      return 'O servidor está iniciando. Aguarde alguns segundos e tente de novo.';
    }
    const msg = err.error && typeof err.error === 'object' ? err.error.error : null;
    if (typeof msg === 'string' && msg.trim()) return msg;
  }
  return 'Não foi possível salvar o livro.';
}

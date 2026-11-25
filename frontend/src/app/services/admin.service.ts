import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';

/** Relatório 1 — totalização em várias tabelas (posts, comentários e reações por membro). */
export interface LinhaTotalizacao {
  usuarioId: number;
  autorNome: string;
  email: string;
  totalPosts: number;
  totalComentarios: number;
  totalReacoes: number;
}

/** Relatório 2 — agrupamento (GROUP BY + HAVING): livros e capítulos por autor. */
export interface LinhaAgrupamento {
  autorNome: string;
  totalLivros: number;
  totalCapitulos: number;
}

/** Relatório 3 — pivot: reações de cada post em colunas. */
export interface LinhaPivot {
  postId: number;
  postTrecho: string;
  autorNome: string;
  totalAmem: number;
  totalCurtir: number;
  totalOrando: number;
  totalReacoesGerais: number;
}

export interface Relatorios {
  totalizacao: LinhaTotalizacao[];
  agrupamento: LinhaAgrupamento[];
  pivot: LinhaPivot[];
}

export interface CidadeAdmin {
  id: number;
  nome: string;
  uf: string;
  membros: number;
}

export interface MembroAdmin {
  id: number;
  nome: string;
  email: string;
  papel: 'PASTOR' | 'MEMBRO';
  cidade: string;
  uf: string;
  membroDesde: string;
}

export interface MembroPayload {
  nome: string;
  papel: 'PASTOR' | 'MEMBRO';
  cidade: string;
  uf: string;
  email?: string;
  senha?: string;
}

export const UFS_BRASIL = [
  'AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA', 'MT', 'MS', 'MG', 'PA',
  'PB', 'PR', 'PE', 'PI', 'RJ', 'RN', 'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO',
];

/** Chamadas da área de Administração (só o Pastor; o backend confere o papel). */
@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly opts = { withCredentials: true };

  relatorios(): Promise<Relatorios> {
    return this.chamar(this.http.get<Relatorios>('/api/relatorios', this.opts));
  }

  listarCidades(busca = ''): Promise<CidadeAdmin[]> {
    const termo = busca.trim();
    const url = termo ? `/api/cidades?busca=${encodeURIComponent(termo)}` : '/api/cidades';
    return this.chamar(this.http.get<CidadeAdmin[]>(url, this.opts));
  }

  criarCidade(nome: string, uf: string): Promise<CidadeAdmin> {
    return this.chamar(this.http.post<CidadeAdmin>('/api/cidades', { nome, uf }, this.opts));
  }

  atualizarCidade(id: number, nome: string, uf: string): Promise<CidadeAdmin> {
    return this.chamar(this.http.put<CidadeAdmin>(`/api/cidades/${id}`, { nome, uf }, this.opts));
  }

  excluirCidade(id: number): Promise<unknown> {
    return this.chamar(this.http.delete(`/api/cidades/${id}`, this.opts));
  }

  listarMembros(busca = ''): Promise<MembroAdmin[]> {
    const termo = busca.trim();
    const url = termo ? `/api/membros?busca=${encodeURIComponent(termo)}` : '/api/membros';
    return this.chamar(this.http.get<MembroAdmin[]>(url, this.opts));
  }

  criarMembro(dados: MembroPayload): Promise<MembroAdmin> {
    return this.chamar(this.http.post<MembroAdmin>('/api/membros', dados, this.opts));
  }

  atualizarMembro(id: number, dados: MembroPayload): Promise<MembroAdmin> {
    return this.chamar(this.http.put<MembroAdmin>(`/api/membros/${id}`, dados, this.opts));
  }

  excluirMembro(id: number): Promise<unknown> {
    return this.chamar(this.http.delete(`/api/membros/${id}`, this.opts));
  }

  /** Converte a falha HTTP em um Error com a mensagem do backend (campo "error"). */
  private async chamar<T>(req: import('rxjs').Observable<T>): Promise<T> {
    try {
      return await firstValueFrom(req);
    } catch (err) {
      throw new Error(mensagemDeErro(err));
    }
  }
}

function mensagemDeErro(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    const msg = err.error && typeof err.error === 'object' ? err.error.error : null;
    if (typeof msg === 'string' && msg.trim()) return msg;
    if (err.status === 401) return 'Sua sessão expirou. Saia e entre de novo.';
    if (err.status === 403) return 'Somente o Pastor pode acessar a Administração.';
    if ([0, 502, 503, 504].includes(err.status)) {
      return 'O servidor está iniciando. Aguarde alguns segundos e tente de novo.';
    }
    return `Erro ${err.status} ao falar com o servidor.`;
  }
  return 'Não foi possível completar a ação.';
}

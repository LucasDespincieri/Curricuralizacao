import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { effect, inject, Injectable, signal } from '@angular/core';
import { catchError, map, Observable, of } from 'rxjs';
import { Usuario } from '../models/community.models';

export interface AuthResult {
  ok: boolean;
  error?: string;
  offline?: boolean;
}

const USER_KEY = 'base_user_v1';

// O backend gratuito "dorme" quando fica sem uso e leva cerca de um minuto para acordar.
const SERVIDOR_INDISPONIVEL =
  'O servidor está iniciando. Aguarde alguns segundos e tente de novo.';


function loadUser(): Usuario | null {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw !== null ? (JSON.parse(raw) as Usuario | null) : null;
  } catch {
    return null;
  }
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  readonly currentUser = signal<Usuario | null>(loadUser());

  constructor() {
    effect(() => {
      try {
        localStorage.setItem(USER_KEY, JSON.stringify(this.currentUser()));
      } catch {
        // ignore storage quota errors
      }
    });

    this.refreshSession();
  }

  refreshSession(): void {
    this.http
      .get<Usuario>('/api/auth/me', { withCredentials: true })
      .pipe(
        catchError((err: HttpErrorResponse) => {
          if (err.status === 401) {
            this.currentUser.set(null);
          }
          return of(null);
        }),
      )
      .subscribe((user) => {
        if (user) {
          this.currentUser.set(user);
        }
      });
  }

  login(email: string, senha: string): Observable<AuthResult> {
    const cleanEmail = email.trim().toLowerCase();
    if (!cleanEmail) {
      return of({ ok: false, error: 'Informe um e-mail válido.' });
    }

    return this.http
      .post<Usuario>('/api/auth/login', { email: cleanEmail, senha }, { withCredentials: true })
      .pipe(
        map((user) => {
          this.currentUser.set(user);
          return { ok: true };
        }),
        catchError((err: HttpErrorResponse) => {
          if (this.isBackendOffline(err)) {
            return of({ ok: false, offline: true, error: SERVIDOR_INDISPONIVEL });
          }
          return of({
            ok: false,
            error: this.extractError(err, 'E-mail ou senha inválidos.'),
          });
        }),
      );
  }

  register(dados: {
    nome: string;
    email: string;
    senha?: string;
    nascimento: string;
    bairro: string;
    cidade: string;
    uf: string;
  }): Observable<AuthResult> {
    return this.http
      .post<Usuario>(
        '/api/auth/register',
        {
          nome: dados.nome,
          email: dados.email,
          senha: dados.senha ?? '',
          nascimento: dados.nascimento,
          bairro: dados.bairro,
        },
        { withCredentials: true },
      )
      .pipe(
        map((user) => {
          this.currentUser.set(user);
          return { ok: true };
        }),
        catchError((err: HttpErrorResponse) => {
          if (this.isBackendOffline(err)) {
            return of({ ok: false, offline: true, error: SERVIDOR_INDISPONIVEL });
          }
          return of({
            ok: false,
            error: this.extractError(err, 'Não foi possível concluir o cadastro.'),
          });
        }),
      );
  }

  private isBackendOffline(err: HttpErrorResponse): boolean {
    return err.status === 0 || err.status === 502 || err.status === 503 || err.status === 504;
  }

  private extractError(err: HttpErrorResponse, fallback: string): string {
    if (err.error && typeof err.error === 'object' && 'error' in err.error) {
      const msg = (err.error as { error?: unknown }).error;
      if (typeof msg === 'string' && msg.trim()) return msg;
    }
    return fallback;
  }

  updateProfile(patch: Partial<Pick<Usuario, 'nome' | 'email' | 'nascimento'>> & {
    bairro?: string;
    cidade?: string;
    uf?: string;
  }): void {
    const user = this.currentUser();
    if (!user) return;

    this.http
      .put<Usuario>(
        '/api/auth/profile',
        { nome: patch.nome, bairro: patch.bairro },
        { withCredentials: true },
      )
      .pipe(catchError(() => of(null)))
      .subscribe((updated) => {
        if (updated) {
          this.currentUser.set(updated);
        }
      });

    this.currentUser.set({
      ...user,
      nome: patch.nome ?? user.nome,
      email: patch.email ?? user.email,
      nascimento: patch.nascimento ?? user.nascimento,
      localizacao: {
        ...user.localizacao,
        bairro: patch.bairro ?? user.localizacao.bairro,
        cidade: patch.cidade ?? user.localizacao.cidade,
        uf: patch.uf ?? user.localizacao.uf,
      },
    });
  }

  logout(): void {
    this.http
      .post('/api/auth/logout', {}, { withCredentials: true })
      .pipe(catchError(() => of(null)))
      .subscribe();
    this.currentUser.set(null);
  }
}

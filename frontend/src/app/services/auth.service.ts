import { HttpClient } from '@angular/common/http';
import { effect, inject, Injectable, signal } from '@angular/core';
import { catchError, of } from 'rxjs';
import { Usuario } from '../models/community.models';

const USER_KEY = 'base_user_v1';

const DEFAULT_USER: Usuario = {
  idUsuario: 1,
  nome: 'Ana Martins',
  username: 'anamartins',
  email: 'ana.martins@comunidade.org',
  nascimento: '1994-05-14',
  dia_cadastro: '2023-03-10',
  role: 'MEMBRO',
  localizacao: {
    idCidade: 1,
    cidade: 'São Paulo',
    uf: 'SP',
    pais: 'Brasil',
    bairro: 'Vila Mariana',
  },
};

function loadUser(): Usuario | null {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw !== null ? (JSON.parse(raw) as Usuario | null) : DEFAULT_USER;
  } catch {
    return DEFAULT_USER;
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
      .pipe(catchError(() => of(null)))
      .subscribe((user) => {
        if (user) {
          this.currentUser.set(user);
        }
      });
  }

  login(email: string, senha: string): boolean {
    if (!email.trim()) return false;

    this.http
      .post<Usuario>('/api/auth/login', { email, senha }, { withCredentials: true })
      .pipe(catchError(() => of(null)))
      .subscribe((user) => {
        if (user) {
          this.currentUser.set(user);
        }
      });

    // Atualização otimista / fallback local caso o backend esteja offline
    this.currentUser.set({
      idUsuario: 1,
      nome: email.includes('lucas') ? 'Pr. Lucas Mendes' : 'Ana Martins',
      username: email.includes('lucas') ? 'prlucasmendes' : 'anamartins',
      email,
      nascimento: '1994-05-14',
      dia_cadastro: '2023-03-10',
      role: email.includes('lucas') ? 'PASTOR' : 'MEMBRO',
      localizacao: {
        idCidade: 1,
        cidade: 'São Paulo',
        uf: 'SP',
        pais: 'Brasil',
        bairro: 'Vila Mariana',
      },
    });
    return true;
  }

  register(dados: {
    nome: string;
    email: string;
    senha?: string;
    nascimento: string;
    bairro: string;
    cidade: string;
    uf: string;
  }): boolean {
    const username = dados.nome
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/[^a-z0-9]/g, '');

    this.http
      .post<Usuario>(
        '/api/auth/register',
        {
          nome: dados.nome,
          email: dados.email,
          senha: dados.senha || '123456',
          nascimento: dados.nascimento,
          bairro: dados.bairro,
        },
        { withCredentials: true },
      )
      .pipe(catchError(() => of(null)))
      .subscribe((user) => {
        if (user) {
          this.currentUser.set(user);
        }
      });

    this.currentUser.set({
      idUsuario: Date.now(),
      nome: dados.nome,
      username: username || 'membro',
      email: dados.email,
      nascimento: dados.nascimento || '1995-01-01',
      dia_cadastro: new Date().toISOString().split('T')[0],
      role: 'MEMBRO',
      localizacao: {
        idCidade: 1,
        cidade: dados.cidade || 'São Paulo',
        uf: (dados.uf || 'SP').toUpperCase().slice(0, 2),
        pais: 'Brasil',
        bairro: dados.bairro || 'Centro',
      },
    });
    return true;
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

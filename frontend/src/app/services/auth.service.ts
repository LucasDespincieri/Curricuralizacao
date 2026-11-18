import { Injectable, signal } from '@angular/core';
import { Usuario } from '../models/community.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly currentUser = signal<Usuario | null>({
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
  });

  login(email: string, _senha: string): boolean {
    if (!email.trim()) return false;
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
    this.currentUser.set(null);
  }
}

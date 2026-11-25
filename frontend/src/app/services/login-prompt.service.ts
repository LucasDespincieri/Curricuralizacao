import { effect, inject, Injectable, signal, untracked } from '@angular/core';
import { AuthService } from './auth.service';
import { SessaoService } from './sessao.service';

/**
 * Controla o pop-up "Entre para continuar".
 *
 * Use `exigirLogin()` antes de qualquer ação que precise de conta (reagir, comentar, salvar...):
 * se o usuário estiver logado, retorna true e a ação segue; se não, abre o pop-up e retorna false.
 */
@Injectable({ providedIn: 'root' })
export class LoginPromptService {
  private readonly auth = inject(AuthService);

  readonly aberto = signal(false);
  readonly motivo = signal('');

  constructor() {
    // Sessão expirou no servidor (ex.: deploy e a pessoa sem o cookie "lembrar de mim"):
    // a tela deixa de mostrar a pessoa como logada e pede para entrar de novo.
    const sessao = inject(SessaoService);
    effect(() => {
      if (!sessao.expirou()) return;
      untracked(() => {
        if (!this.auth.currentUser()) return;
        this.auth.currentUser.set(null);
        this.motivo.set('Sua sessão expirou. Entre de novo para continuar.');
        this.aberto.set(true);
      });
    });
  }

  exigirLogin(motivo = 'Entre na sua conta para continuar.'): boolean {
    if (this.auth.currentUser()) return true;
    this.motivo.set(motivo);
    this.aberto.set(true);
    return false;
  }

  fechar(): void {
    this.aberto.set(false);
  }
}

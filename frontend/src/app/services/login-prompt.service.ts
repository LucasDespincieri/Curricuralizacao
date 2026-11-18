import { inject, Injectable, signal } from '@angular/core';
import { AuthService } from './auth.service';

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

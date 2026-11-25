import { Injectable, signal } from '@angular/core';

const CHAVE = 'base_tema';

/**
 * Modo claro/escuro. A escolha fica salva no navegador (localStorage) e o script do index.html
 * reaplica o tema antes do site carregar, então ele continua igual ao atualizar a página.
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  readonly escuro = signal(document.documentElement.classList.contains('dark'));

  alternar(): void {
    this.definir(!this.escuro());
  }

  definir(escuro: boolean): void {
    this.escuro.set(escuro);
    document.documentElement.classList.toggle('dark', escuro);
    try {
      localStorage.setItem(CHAVE, escuro ? 'escuro' : 'claro');
    } catch {
      // navegador sem localStorage: o tema vale só até fechar a aba
    }
  }
}

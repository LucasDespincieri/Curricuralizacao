import { Component, HostListener, inject } from '@angular/core';
import { Router } from '@angular/router';
import { LucideLogIn, LucideX } from '@lucide/angular';
import { LoginPromptService } from '../../services/login-prompt.service';

/** Pop-up exibido quando um visitante sem login tenta fazer algo que exige conta. */
@Component({
  selector: 'app-login-prompt',
  standalone: true,
  imports: [LucideLogIn, LucideX],
  template: `
    @if (prompt.aberto()) {
      <div
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 px-4"
        (click)="prompt.fechar()"
      >
        <div
          role="dialog"
          aria-modal="true"
          aria-labelledby="login-prompt-titulo"
          class="relative w-full max-w-sm rounded-2xl bg-white p-6 shadow-xl"
          (click)="$event.stopPropagation()"
        >
          <button
            type="button"
            (click)="prompt.fechar()"
            aria-label="Fechar"
            class="absolute right-3 top-3 rounded-lg p-1.5 text-slate-400 transition hover:bg-slate-100 hover:text-slate-700"
          >
            <svg lucideX [size]="16"></svg>
          </button>

          <div
            class="mb-4 flex h-10 w-10 items-center justify-center rounded-xl bg-slate-800 text-white"
          >
            <svg lucideLogIn [size]="18"></svg>
          </div>
          <h2 id="login-prompt-titulo" class="text-lg font-bold text-slate-900">
            Entre para continuar
          </h2>
          <p class="mt-1.5 text-sm leading-relaxed text-slate-600">{{ prompt.motivo() }}</p>

          <div class="mt-6 flex flex-col gap-2">
            <button
              type="button"
              (click)="irPara('LOGIN')"
              class="w-full rounded-full bg-slate-800 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-slate-900"
            >
              Entrar
            </button>
            <button
              type="button"
              (click)="irPara('REGISTER')"
              class="w-full rounded-full border border-slate-300 px-4 py-2.5 text-sm font-semibold text-slate-800 transition hover:bg-slate-50"
            >
              Criar conta
            </button>
            <button
              type="button"
              (click)="prompt.fechar()"
              class="w-full px-4 py-2 text-xs font-medium text-slate-500 transition hover:text-slate-800"
            >
              Agora não
            </button>
          </div>
        </div>
      </div>
    }
  `,
})
export class LoginPromptComponent {
  protected readonly prompt = inject(LoginPromptService);
  private readonly router = inject(Router);

  @HostListener('document:keydown.escape')
  aoApertarEsc(): void {
    this.prompt.fechar();
  }

  irPara(modo: 'LOGIN' | 'REGISTER'): void {
    this.prompt.fechar();
    this.router.navigate(['/login'], {
      queryParams: { redirect: this.router.url, ...(modo === 'REGISTER' ? { modo: 'cadastro' } : {}) },
    });
  }
}

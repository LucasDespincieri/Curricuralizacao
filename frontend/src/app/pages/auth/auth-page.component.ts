import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { LucideArrowRight, LucideBookOpen } from '@lucide/angular';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-auth-page',
  standalone: true,
  imports: [FormsModule, LucideBookOpen, LucideArrowRight],
  template: `
    <div class="grid min-h-screen lg:grid-cols-2">
      <!-- Lado Esquerdo: Boas-vindas -->
      <section
        class="relative hidden flex-col justify-between bg-slate-900 p-10 text-white lg:flex"
      >
        <div class="flex items-center gap-3">
          <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-white/10">
            <svg lucideBookOpen [size]="20"></svg>
          </div>
          <div>
            <p class="text-base font-bold leading-tight">Comunidade</p>
            <p class="text-[11px] tracking-widest text-slate-300 uppercase">DA FÉ · BASE!</p>
          </div>
        </div>

        <div class="max-w-lg">
          <p class="text-xs font-semibold tracking-widest text-slate-300 uppercase">
            — Um lugar para pertencer
          </p>
          <h1 class="mt-4 text-4xl font-bold leading-tight tracking-tight">
            Conectando a comunidade através da comunhão e da palavra.
          </h1>
          <p class="mt-4 text-sm leading-relaxed text-slate-300">
            Compartilhe aprendizados, encontre acolhimento e caminhe junto com pessoas que também
            buscam viver uma fé prática.
          </p>
        </div>

        <p class="text-xs text-slate-400">Uma comunidade para estudar, conversar e crescer juntos.</p>
      </section>

      <!-- Lado Direito: Formulário -->
      <section class="flex items-center justify-center bg-[#FAF9F6] px-6 py-12">
        <div class="w-full max-w-md">
          <p class="text-[11px] font-semibold tracking-widest text-slate-400 uppercase">
            {{ mode() === 'LOGIN' ? 'Bem-vindo de volta' : 'Faça parte' }}
          </p>
          <h2 class="mt-1 text-3xl font-bold tracking-tight text-slate-900">
            {{ mode() === 'LOGIN' ? 'Entre na comunidade' : 'Crie sua conta' }}
          </h2>
          <p class="mt-1 text-sm text-slate-500">
            {{
              mode() === 'LOGIN'
                ? 'Continue sua jornada de comunhão e aprendizado.'
                : 'Preencha seus dados para participar dos estudos e do feed.'
            }}
          </p>

          <!-- Seletor Entrar / Cadastrar-se -->
          <div class="mt-6 grid grid-cols-2 rounded-xl border border-slate-200 bg-slate-100 p-1">
            <button
              type="button"
              (click)="mode.set('LOGIN')"
              class="rounded-lg py-2 text-xs font-semibold transition"
              [class.bg-white]="mode() === 'LOGIN'"
              [class.text-slate-900]="mode() === 'LOGIN'"
              [class.shadow-xs]="mode() === 'LOGIN'"
              [class.text-slate-500]="mode() !== 'LOGIN'"
            >
              Entrar
            </button>
            <button
              type="button"
              (click)="mode.set('REGISTER')"
              class="rounded-lg py-2 text-xs font-semibold transition"
              [class.bg-white]="mode() === 'REGISTER'"
              [class.text-slate-900]="mode() === 'REGISTER'"
              [class.shadow-xs]="mode() === 'REGISTER'"
              [class.text-slate-500]="mode() !== 'REGISTER'"
            >
              Cadastrar-se
            </button>
          </div>

          @if (mode() === 'LOGIN') {
            <form (ngSubmit)="submitLogin()" class="mt-6 space-y-4">
              <div>
                <label class="mb-1.5 block text-xs font-semibold text-slate-700">E-mail</label>
                <input
                  type="email"
                  name="email"
                  [(ngModel)]="loginEmail"
                  required
                  placeholder="voce@email.com"
                  class="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm text-slate-900 focus:border-slate-400 focus:outline-none"
                />
              </div>

              <div>
                <label class="mb-1.5 block text-xs font-semibold text-slate-700">Senha</label>
                <input
                  type="password"
                  name="senha"
                  [(ngModel)]="loginSenha"
                  required
                  minlength="6"
                  placeholder="Mínimo de 6 caracteres"
                  class="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm text-slate-900 focus:border-slate-400 focus:outline-none"
                />
              </div>

              <button
                type="submit"
                class="mt-2 flex w-full items-center justify-center gap-2 rounded-xl bg-slate-800 px-4 py-3 text-sm font-semibold text-white transition hover:bg-slate-900"
              >
                <span>Entrar na comunidade</span>
                <svg lucideArrowRight [size]="16"></svg>
              </button>
            </form>
          } @else {
            <form (ngSubmit)="submitRegister()" class="mt-6 space-y-3.5">
              <div>
                <label class="mb-1 block text-xs font-semibold text-slate-700">Nome completo</label>
                <input
                  type="text"
                  name="nome"
                  [(ngModel)]="regNome"
                  required
                  placeholder="Seu nome"
                  class="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2 text-sm focus:border-slate-400 focus:outline-none"
                />
              </div>

              <div>
                <label class="mb-1 block text-xs font-semibold text-slate-700">E-mail</label>
                <input
                  type="email"
                  name="regEmail"
                  [(ngModel)]="regEmail"
                  required
                  placeholder="voce@email.com"
                  class="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2 text-sm focus:border-slate-400 focus:outline-none"
                />
              </div>

              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="mb-1 block text-xs font-semibold text-slate-700">Nascimento</label>
                  <input
                    type="date"
                    name="nascimento"
                    [(ngModel)]="regNascimento"
                    class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm focus:border-slate-400 focus:outline-none"
                  />
                </div>
                <div>
                  <label class="mb-1 block text-xs font-semibold text-slate-700">Bairro</label>
                  <input
                    type="text"
                    name="bairro"
                    [(ngModel)]="regBairro"
                    placeholder="Vila Mariana"
                    class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm focus:border-slate-400 focus:outline-none"
                  />
                </div>
              </div>

              <div class="grid grid-cols-3 gap-3">
                <div class="col-span-2">
                  <label class="mb-1 block text-xs font-semibold text-slate-700">Cidade</label>
                  <input
                    type="text"
                    name="cidade"
                    [(ngModel)]="regCidade"
                    placeholder="São Paulo"
                    class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm focus:border-slate-400 focus:outline-none"
                  />
                </div>
                <div>
                  <label class="mb-1 block text-xs font-semibold text-slate-700">UF</label>
                  <input
                    type="text"
                    name="uf"
                    maxlength="2"
                    [(ngModel)]="regUf"
                    placeholder="SP"
                    class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm uppercase focus:border-slate-400 focus:outline-none"
                  />
                </div>
              </div>

              <button
                type="submit"
                class="mt-2 flex w-full items-center justify-center gap-2 rounded-xl bg-slate-800 px-4 py-3 text-sm font-semibold text-white transition hover:bg-slate-900"
              >
                <span>Criar conta e entrar</span>
                <svg lucideArrowRight [size]="16"></svg>
              </button>
            </form>
          }
        </div>
      </section>
    </div>
  `,
})
export class AuthPageComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly mode = signal<'LOGIN' | 'REGISTER'>('LOGIN');

  loginEmail = 'ana.martins@comunidade.org';
  loginSenha = '';

  regNome = '';
  regEmail = '';
  regNascimento = '';
  regBairro = '';
  regCidade = '';
  regUf = '';

  submitLogin(): void {
    if (this.auth.login(this.loginEmail, this.loginSenha)) {
      this.router.navigate(['/inicio']);
    }
  }

  submitRegister(): void {
    if (!this.regNome.trim() || !this.regEmail.trim()) return;
    this.auth.register({
      nome: this.regNome,
      email: this.regEmail,
      nascimento: this.regNascimento,
      bairro: this.regBairro,
      cidade: this.regCidade,
      uf: this.regUf,
    });
    this.router.navigate(['/inicio']);
  }
}

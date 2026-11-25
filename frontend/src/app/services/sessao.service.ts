import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { catchError, throwError } from 'rxjs';

/**
 * Avisa quando o servidor diz que a sessão acabou (HTTP 401) no meio do uso.
 * Não depende de nenhum outro serviço, para o interceptor poder usá-lo sem dependência circular.
 * Quem reage é o LoginPromptService: tira o usuário da tela e abre o "Entre para continuar".
 */
@Injectable({ providedIn: 'root' })
export class SessaoService {
  /** Aumenta a cada 401 recebido (o valor em si não importa, só a mudança). */
  readonly expirou = signal(0);

  avisarExpirada(): void {
    this.expirou.update((n) => n + 1);
  }
}

/** Rotas em que o 401 é esperado e já tratado (senha errada no login, checagem inicial de sessão). */
const IGNORAR = ['/api/auth/login', '/api/auth/register', '/api/auth/me'];

export const sessaoInterceptor: HttpInterceptorFn = (req, next) => {
  const sessao = inject(SessaoService);
  return next(req).pipe(
    catchError((err: unknown) => {
      if (
        err instanceof HttpErrorResponse &&
        err.status === 401 &&
        req.url.includes('/api/') &&
        !IGNORAR.some((rota) => req.url.includes(rota))
      ) {
        sessao.avisarExpirada();
      }
      return throwError(() => err);
    }),
  );
};

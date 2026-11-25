import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';

/** Dados devolvidos pelo backend em POST /api/uploads/assinatura. */
interface AssinaturaUpload {
  cloudName: string;
  apiKey: string;
  timestamp: number;
  folder: string;
  signature: string;
}

export const TIPOS_IMAGEM_ACEITOS = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];
export const TAMANHO_MAXIMO_IMAGEM = 5 * 1024 * 1024; // 5 MB

/**
 * Envia imagens direto do navegador para o Cloudinary.
 *
 * O arquivo não passa pelo backend: o backend só gera uma assinatura (o api_secret fica lá),
 * e o navegador manda o arquivo para o Cloudinary, que devolve a URL pública da imagem.
 */
@Injectable({ providedIn: 'root' })
export class UploadService {
  private readonly http = inject(HttpClient);

  /** Retorna uma mensagem de erro se o arquivo não puder ser enviado, ou null se estiver ok. */
  validarImagem(arquivo: File): string | null {
    if (!TIPOS_IMAGEM_ACEITOS.includes(arquivo.type)) {
      return 'Formato não suportado. Use JPG, PNG, WEBP ou GIF.';
    }
    if (arquivo.size > TAMANHO_MAXIMO_IMAGEM) {
      return 'A imagem deve ter no máximo 5 MB.';
    }
    return null;
  }

  /** Apaga do Cloudinary uma imagem enviada que não chegou a ser usada em nenhum post. */
  descartarImagem(url: string): void {
    this.http
      .request('DELETE', '/api/uploads/imagem', { body: { url }, withCredentials: true })
      .subscribe({ error: () => undefined });
  }

  /** Envia a imagem e devolve a URL pública (https) gerada pelo Cloudinary. */
  async enviarImagemPost(arquivo: File): Promise<string> {
    let assinatura: AssinaturaUpload;
    try {
      assinatura = await firstValueFrom(
        this.http.post<AssinaturaUpload>('/api/uploads/assinatura', {}, { withCredentials: true }),
      );
    } catch (err) {
      throw new Error(mensagemErroAssinatura(err));
    }

    const form = new FormData();
    form.append('file', arquivo);
    form.append('api_key', assinatura.apiKey);
    form.append('timestamp', String(assinatura.timestamp));
    form.append('folder', assinatura.folder);
    form.append('signature', assinatura.signature);

    let resposta: Response;
    try {
      resposta = await fetch(
        `https://api.cloudinary.com/v1_1/${assinatura.cloudName}/image/upload`,
        { method: 'POST', body: form },
      );
    } catch {
      throw new Error('Não foi possível conectar ao serviço de imagens. Verifique sua internet.');
    }
    const dados = await resposta.json().catch(() => null);
    if (!resposta.ok || typeof dados?.secure_url !== 'string') {
      throw new Error(dados?.error?.message ?? 'Falha ao enviar a imagem.');
    }
    return dados.secure_url as string;
  }
}

/** Traduz a falha do pedido de assinatura em uma mensagem que ajude a descobrir o problema. */
function mensagemErroAssinatura(err: unknown): string {
  if (!(err instanceof HttpErrorResponse)) return 'Não foi possível preparar o envio da imagem.';
  const doServidor =
    err.error && typeof err.error === 'object' && typeof err.error.error === 'string'
      ? (err.error.error as string)
      : null;
  switch (err.status) {
    case 401:
      return 'Sua sessão expirou. Saia e entre de novo para enviar imagens.';
    case 404:
      return 'O servidor ainda não tem o envio de imagens (versão antiga no ar).';
    case 503:
      // Com mensagem do nosso backend: falta configurar o Cloudinary. Sem mensagem: Render acordando.
      return doServidor ?? 'O servidor está iniciando. Aguarde alguns segundos e tente de novo.';
    case 0:
    case 502:
    case 504:
      return 'O servidor está iniciando. Aguarde alguns segundos e tente de novo.';
    default:
      return doServidor ?? `Erro ${err.status} ao preparar o envio da imagem.`;
  }
}

/**
 * Pede ao Cloudinary uma versão otimizada da imagem (formato e qualidade automáticos,
 * largura máxima de 1000px). Economiza banda e créditos do plano gratuito.
 */
export function urlImagemOtimizada(url: string | null | undefined): string | null {
  if (!url) return null;
  return url.replace('/image/upload/', '/image/upload/f_auto,q_auto,c_limit,w_1000/');
}

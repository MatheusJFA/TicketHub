import { ToastService } from './ui/toast.service';

/**
 * Compartilha um link via Web Share API (mobile) ou copia para a área de
 * transferência como fallback — sempre com feedback via toast.
 */
export async function shareLink(
  url: string,
  title: string,
  text: string,
  toast: ToastService,
): Promise<void> {
  try {
    if (typeof navigator !== 'undefined' && 'share' in navigator) {
      await navigator.share({ title, text, url });
      return;
    }
  } catch {
    // Usuário cancelou o sheet ou falhou: cai no copiar.
  }
  try {
    await navigator.clipboard.writeText(url);
    toast.show(text);
  } catch {
    // clipboard indisponível: nada a fazer
  }
}

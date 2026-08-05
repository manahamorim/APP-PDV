# Como publicar o app no Android (sem instalar nada, sem linha de comando)

Este é um "app" que roda como PWA (Progressive Web App): depois de publicado,
qualquer pessoa com Android + Chrome consegue instalá-lo com um toque, ganha
um ícone na tela inicial, e ele abre em tela cheia — sem barra de endereço,
como um app de verdade. Por baixo dos panos, ele continua usando a mesma
planilha e o mesmo Apps Script de sempre.

Você só precisa de uma conta gratuita no GitHub (github.com). Nenhum comando
de terminal é necessário — tudo pelo site.

## Passo 1 — Criar o repositório

1. Acesse **github.com** e faça login (ou crie uma conta gratuita).
2. Clique no **+** no canto superior direito → **New repository**.
3. Dê um nome, por exemplo `positivacao-app`.
4. Marque como **Public**.
5. Clique em **Create repository**.

## Passo 2 — Enviar os arquivos

1. Na página do repositório recém-criado, clique em **Add file** → **Upload files**.
2. Arraste estes 5 arquivos desta pasta para a janela do navegador:
   - `index.html`
   - `manifest.json`
   - `service-worker.js`
   - `icon-192.png`
   - `icon-512.png`
3. Clique em **Commit changes**.

## Passo 3 — Ativar o GitHub Pages

1. No repositório, vá em **Settings** (aba no topo).
2. No menu lateral, clique em **Pages**.
3. Em "Build and deployment" → **Source**, selecione **Deploy from a branch**.
4. Em "Branch", selecione **main** e a pasta **/ (root)**. Clique em **Save**.
5. Aguarde 1–2 minutos. Recarregue a página — vai aparecer um link no topo,
   algo como:

   `https://SEU-USUARIO.github.io/positivacao-app/`

## Passo 4 — Instalar no Android

1. Abra esse link no **Chrome do Android**.
2. Toque no menu (⋮) no canto superior direito.
3. Toque em **"Instalar app"** (ou "Adicionar à tela inicial").
4. Pronto — o ícone "realme" aparece na tela inicial e abre em tela cheia.

## Observações

- Qualquer atualização futura no formulário (novo campo, novo material etc.)
  precisa ser feita em dois lugares: no `App.html` do Apps Script (versão
  dentro do Google) **e** no `index.html` deste pacote (repita o Passo 2 —
  Upload files — para atualizar no GitHub). Se quiser, posso automatizar
  isso da próxima vez.
- O backend continua 100% no Google Apps Script/Sheets — este pacote é só
  a "casca" visual instalável. Nenhum dado é armazenado no GitHub.
- Se quiser um domínio próprio (ex: `positivacao.suaempresa.com`) em vez do
  link `github.io`, dá pra configurar depois em Settings → Pages → Custom domain.

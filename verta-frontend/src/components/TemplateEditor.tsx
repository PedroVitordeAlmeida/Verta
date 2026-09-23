import { forwardRef, useImperativeHandle } from 'react'
import { EditorContent, useEditor } from '@tiptap/react'
import StarterKit from '@tiptap/starter-kit'
import TextAlign from '@tiptap/extension-text-align'
import Underline from '@tiptap/extension-underline'

export interface TemplateEditorHandle {
  /** Substitui todo o conteudo do editor (ex: depois de gerar um template com IA). */
  setContent: (html: string) => void
}

/**
 * Editor rich-text (Tiptap) usado no cadastro/edicao de templates. Salva o conteudo como
 * HTML puro (editor.getHTML()) - ver utils/formatarConteudo.tsx (ConteudoRenderizado,
 * converterMarkupAntigoParaHtml) para como esse HTML e exibido/exportado depois, e como um
 * template no formato antigo (marcacoes de texto) e convertido ao ser aberto aqui.
 *
 * {{variavel}} continua sendo digitado como texto normal - nao e um recurso do editor.
 */
export const TemplateEditor = forwardRef<TemplateEditorHandle, { conteudoInicial: string; onChange: (html: string) => void }>(
  function TemplateEditor({ conteudoInicial, onChange }, ref) {
    const editor = useEditor({
      extensions: [
        StarterKit.configure({ heading: { levels: [1] } }),
        Underline,
        TextAlign.configure({ types: ['heading', 'paragraph'], alignments: ['left', 'center', 'right', 'justify'] })
      ],
      content: conteudoInicial,
      onUpdate: ({ editor }) => onChange(editor.getHTML())
    })

    useImperativeHandle(
      ref,
      () => ({
        setContent: (html: string) => {
          editor?.commands.setContent(html, false)
        }
      }),
      [editor]
    )

    if (!editor) return null

    function botao(ativo: boolean, titulo: string, conteudo: string, onClick: () => void) {
      return (
        <button type="button" className={`format-btn${ativo ? ' active' : ''}`} title={titulo} onClick={onClick}>
          {conteudo}
        </button>
      )
    }

    return (
      <div className="tiptap-wrapper">
        <div className="format-toolbar">
          {botao(editor.isActive('bold'), 'Negrito', 'B', () => editor.chain().focus().toggleBold().run())}
          {botao(editor.isActive('italic'), 'Itálico', 'I', () => editor.chain().focus().toggleItalic().run())}
          {botao(editor.isActive('underline'), 'Sublinhado', 'S', () => editor.chain().focus().toggleUnderline().run())}
          {botao(editor.isActive('bulletList'), 'Lista com marcadores', '• Lista', () =>
            editor.chain().focus().toggleBulletList().run()
          )}
          {botao(editor.isActive('heading', { level: 1 }), 'Título', '# Título', () =>
            editor.chain().focus().toggleHeading({ level: 1 }).run()
          )}
          <span className="format-toolbar-separador" />
          {botao(editor.isActive({ textAlign: 'left' }), 'Alinhar à esquerda', '⯇ Esquerda', () =>
            editor.chain().focus().setTextAlign('left').run()
          )}
          {botao(editor.isActive({ textAlign: 'center' }), 'Centralizar', '⯃ Centro', () =>
            editor.chain().focus().setTextAlign('center').run()
          )}
          {botao(editor.isActive({ textAlign: 'right' }), 'Alinhar à direita', '⯈ Direita', () =>
            editor.chain().focus().setTextAlign('right').run()
          )}
          {botao(editor.isActive({ textAlign: 'justify' }), 'Justificar', '☰ Justificar', () =>
            editor.chain().focus().setTextAlign('justify').run()
          )}
        </div>
        <EditorContent editor={editor} className="editor-body tiptap-content" />
      </div>
    )
  }
)

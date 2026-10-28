import { useEffect, useState } from 'react';
import ReactMarkdown from 'react-markdown';
import {
  ArrowRight, BookOpenText, Check, Clipboard, Clock3, Download, FileText,
  Layers3, LoaderCircle, Newspaper, Sparkles, WandSparkles, Zap, ShieldCheck,
} from 'lucide-react';

type ApiResponse = {
  summary: string;
  provider: string;
  requestId: string;
  fallbackUsed: boolean;
  processingTimeMs: number;
  outputCharacters: number;
  message?: string;
};
type SystemStatus = {
  status: string;
  configuredProvider: string;
  yandexConfigured: boolean;
  effectiveMode: 'demo' | 'yandex' | string;
  message: string;
};
type Example = { label: string; title: string; source: string; text: string };

const MAX_CHARS = 25000;
const examples: Example[] = [
  {
    label: 'Технологии',
    title: 'Новый центр обработки данных откроют в регионе',
    source: 'Пример редакции',
    text: 'В регионе планируют построить новый центр обработки данных для поддержки цифровых сервисов и корпоративных клиентов. Проект предусматривает несколько очередей строительства и поэтапный ввод инфраструктуры. На первом этапе инвестор намерен подготовить площадку, подключить инженерные сети и установить серверное оборудование. Представители проекта отмечают, что объект должен повысить доступность вычислительных мощностей для местных компаний. Сроки реализации будут уточнены после завершения проектной документации и согласования технических условий.',
  },
  {
    label: 'Экология',
    title: 'Город расширяет сеть пунктов раздельного сбора отходов',
    source: 'Пример редакции',
    text: 'Муниципальные власти объявили о расширении сети пунктов раздельного сбора отходов. Новые контейнеры планируется установить рядом с жилыми кварталами, образовательными учреждениями и общественными пространствами. В первую очередь организаторы намерены собирать бумагу, стекло и отдельные виды пластика. До установки оборудования жителям обещают опубликовать карту пунктов и правила подготовки вторсырья. Эффективность программы будут оценивать по объёму собранных материалов и данным операторов по вывозу отходов.',
  },
  {
    label: 'Наука',
    title: 'Университетская команда представила прототип роботизированной платформы',
    source: 'Пример редакции',
    text: 'Команда исследователей университета представила прототип мобильной роботизированной платформы для лабораторных испытаний. Устройство оснащено датчиками расстояния, камерой и вычислительным модулем, который обрабатывает данные о препятствиях. Сейчас разработчики проверяют устойчивость навигации в помещениях со сложной планировкой и меняющимся освещением. Следующий этап проекта предполагает серию контролируемых испытаний и доработку программного обеспечения. Исследователи подчёркивают, что до практического применения платформе предстоит пройти дополнительные проверки.',
  },
];

export default function App() {
  const [title, setTitle] = useState('');
  const [source, setSource] = useState('');
  const [articleText, setArticleText] = useState('');
  const [length, setLength] = useState('standard');
  const [includeKeyPoints, setIncludeKeyPoints] = useState(true);
  const [answer, setAnswer] = useState<ApiResponse | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [copied, setCopied] = useState(false);
  const [systemStatus, setSystemStatus] = useState<SystemStatus | null>(null);

  useEffect(() => {
    fetch('/api/system/status')
      .then(async (response) => {
        if (!response.ok) throw new Error('Status unavailable');
        return response.json() as Promise<SystemStatus>;
      })
      .then(setSystemStatus)
      .catch(() => setSystemStatus(null));
  }, []);

  function useExample(example: Example) {
    setTitle(example.title);
    setSource(example.source);
    setArticleText(example.text);
    setAnswer(null);
    setError('');
  }

  async function generate() {
    if (articleText.trim().length < 40) {
      setError('Добавьте текст статьи длиной не менее 40 символов. Можно начать с примера ниже.');
      return;
    }
    setLoading(true);
    setError('');
    setAnswer(null);
    setCopied(false);
    try {
      const response = await fetch('/api/articles/summarize', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ title, sourceName: source, articleText, length, includeKeyPoints }),
      });
      const data = await response.json() as ApiResponse;
      if (!response.ok) throw new Error(data.message || 'Не удалось подготовить аннотацию.');
      setAnswer(data);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Не удалось подключиться к серверу.');
    } finally {
      setLoading(false);
    }
  }

  async function copyAnswer() {
    if (!answer) return;
    try {
      await navigator.clipboard.writeText(answer.summary);
      setCopied(true);
      window.setTimeout(() => setCopied(false), 1800);
    } catch {
      setError('Не удалось скопировать результат. Выделите текст вручную.');
    }
  }

  function downloadAnswer() {
    if (!answer) return;
    const blob = new Blob([answer.summary], { type: 'text/markdown;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'newsbrief-summary.md';
    link.click();
    URL.revokeObjectURL(url);
  }

  const modeLabel = systemStatus?.effectiveMode === 'yandex' ? 'Yandex AI подключён' : 'Демо-режим доступен';

  return (
    <main className="shell">
      <header className="topbar">
        <a className="brand" href="#top" aria-label="NewsBrief AI — главная">
          <span className="brand-icon"><Newspaper size={21} /></span>
          News<span className="accent">·</span>Brief <span className="brand-ai">AI</span>
        </a>
        <div className="header-right">
          <span className={`mode-badge ${systemStatus?.effectiveMode === 'yandex' ? 'is-live' : ''}`}>
            <i />{modeLabel}
          </span>
          <a className="docs" href="/swagger-ui.html" target="_blank" rel="noreferrer">API <ArrowRight size={14} /></a>
        </div>
      </header>

      <section className="hero" id="top">
        <div className="eyebrow"><Sparkles size={14} /> AI-РЕДАКТОР НОВОСТЕЙ</div>
        <h1>Важное — <span>короче.</span><br />Смысл — на первом месте.</h1>
        <p>Превратите длинный материал в ясную аннотацию для новостной ленты. Без лишнего шума, выдуманных фактов и потери главной мысли.</p>
        <div className="steps">
          <span><b>01</b> Вставьте статью</span><i />
          <span><b>02</b> Выберите формат</span><i />
          <span><b>03</b> Получите аннотацию</span>
        </div>
      </section>

      <section className="workspace">
        <article className="panel">
          <div className="panel-title">
            <span className="title-icon"><BookOpenText size={19} /></span>
            <div><h2>Исходный материал</h2><p>Добавьте контекст — остальное сделает AI</p></div>
            <small>01</small>
          </div>

          <div className="field">
            <label htmlFor="title">Заголовок статьи <em>НЕОБЯЗАТЕЛЬНО</em></label>
            <input id="title" value={title} onChange={(e) => setTitle(e.target.value)} maxLength={250} placeholder="Например, город запустил новую программу…" />
          </div>
          <div className="field-row">
            <div className="field">
              <label htmlFor="source">Источник <em>НЕОБЯЗАТЕЛЬНО</em></label>
              <input id="source" value={source} onChange={(e) => setSource(e.target.value)} maxLength={150} placeholder="Название издания" />
            </div>
            <div className="field">
              <label htmlFor="length">Формат аннотации</label>
              <select id="length" value={length} onChange={(e) => setLength(e.target.value)}>
                <option value="short">Короткая · для ленты</option>
                <option value="standard">Стандартная · с деталями</option>
              </select>
            </div>
          </div>
          <div className="field">
            <div className="label-row"><label htmlFor="article">Текст статьи <strong>*</strong></label><span className="count">{articleText.length.toLocaleString('ru-RU')} / {MAX_CHARS.toLocaleString('ru-RU')}</span></div>
            <textarea id="article" value={articleText} onChange={(e) => setArticleText(e.target.value)} maxLength={MAX_CHARS} rows={9} placeholder="Вставьте полный текст статьи. AI выделит главное, сохранит важные факты и подготовит краткий пересказ…" />
          </div>
          <label className="check-row" htmlFor="keypoints">
            <input id="keypoints" type="checkbox" checked={includeKeyPoints} onChange={(e) => setIncludeKeyPoints(e.target.checked)} />
            <span className="fake-check"><Check size={13} /></span>
            <span><strong>Добавить ключевые тезисы</strong><small>2–4 пункта для быстрого просмотра редактором</small></span>
          </label>
          <div className="examples">
            <b>ПОПРОБУЙТЕ НА ПРИМЕРЕ</b>
            {examples.map((example) => <button type="button" key={example.label} onClick={() => useExample(example)}>{example.label}</button>)}
          </div>
          {error && <div className="error" role="alert">{error}</div>}
          <button className="generate" onClick={generate} disabled={loading}>
            <span>{loading ? <LoaderCircle className="spin" size={19} /> : <WandSparkles size={19} />}</span>
            {loading ? 'Готовим аннотацию…' : 'Сгенерировать аннотацию'}
            {!loading && <ArrowRight size={18} />}
          </button>
          <p className="privacy"><i /> Текст статьи не попадает в логи приложения</p>
        </article>

        <article className="panel result">
          <div className="panel-title">
            <span className="title-icon purple"><FileText size={19} /></span>
            <div><h2>Результат</h2><p>Предварительная версия для новостной ленты</p></div>
            <small>02</small>
          </div>
          {!answer && !loading && <div className="empty">
            <div className="orb"><WandSparkles size={31} /><span>✳</span></div>
            <h3>Суть новости<br />без лишнего шума</h3>
            <p>Здесь появятся аннотация, ключевые тезисы и информация о режиме генерации. Перед публикацией проверьте факты по первоисточнику.</p>
            <div className="hint">↳ Результат можно скопировать или скачать</div>
          </div>}
          {loading && <div className="empty">
            <div className="loading-orb"><Sparkles size={29} /></div>
            <h3>Выделяем главное</h3>
            <p>Сокращаем материал, сверяемся с исходным текстом и формируем структуру…</p>
            <div className="loading-bars"><i /><i /><i /><i /></div>
          </div>}
          {answer && !loading && <div className="answer">
            <div className="meta">
              <span><Check size={13} /> ГОТОВО</span>
              <small>{answer.provider}</small>
            </div>
            {answer.fallbackUsed && <div className="notice"><Zap size={15} /><span>Использован локальный резервный алгоритм. Для AI-генерации настройте доступ к Yandex AI Studio.</span></div>}
            <div className="markdown"><ReactMarkdown>{answer.summary}</ReactMarkdown></div>
            <div className="result-stats">
              <span><Clock3 size={14} /> {answer.processingTimeMs} мс</span>
              <span><FileText size={14} /> {answer.outputCharacters} симв.</span>
              <span><ShieldCheck size={14} /> {answer.fallbackUsed ? 'Fallback' : 'AI Gateway'}</span>
            </div>
            <div className="request-id">ID запроса: {answer.requestId}</div>
            <div className="actions">
              <button onClick={copyAnswer}><Clipboard size={16} />{copied ? 'Скопировано' : 'Копировать'}</button>
              <button onClick={downloadAnswer}><Download size={16} /> Скачать .md</button>
            </div>
          </div>}
        </article>
      </section>

      <section className="trust-row">
        <div><span><Layers3 size={17} /></span><b>AI Gateway</b><small>Сменяемые LLM-провайдеры</small></div>
        <div><span><ShieldCheck size={17} /></span><b>Защита данных</b><small>Секреты только на backend</small></div>
        <div><span><Zap size={17} /></span><b>Docker-ready</b><small>Один запуск для всего проекта</small></div>
      </section>
      <footer><small>NewsBrief AI · Лабораторная работа № 1 · Вариант 13 <b>✳</b></small><small>AI помогает редактору — фактчекинг остаётся обязательным.</small></footer>
    </main>
  );
}

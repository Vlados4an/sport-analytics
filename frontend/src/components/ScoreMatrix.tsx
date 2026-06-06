import clsx from 'clsx'

interface Props {
  matrix: number[][]
  lambdaHome: number
  muAway: number
}

export default function ScoreMatrix({ matrix, lambdaHome, muAway }: Props) {
  const maxVal = Math.max(...matrix.flat())
  const size = Math.min(matrix.length, 7)

  return (
    <div className="space-y-3">
      <div className="flex items-center gap-5 text-[12px] text-ink-500">
        <span><span className="font-mono font-semibold text-brand-600">&lambda;</span> = {lambdaHome.toFixed(2)} <span className="text-ink-400">(хозяева)</span></span>
        <span><span className="font-mono font-semibold text-brand-600">&mu;</span> = {muAway.toFixed(2)} <span className="text-ink-400">(гости)</span></span>
      </div>

      <div className="overflow-x-auto">
        <table className="border-collapse">
          <thead>
            <tr>
              <th className="w-9 h-9 text-[10px] text-ink-400 font-medium text-right pr-1.5 pb-0.5">H\A</th>
              {Array.from({ length: size }, (_, i) => (
                <th key={i} className="w-[52px] h-9 text-center text-[11px] font-semibold text-ink-500">{i}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {Array.from({ length: size }, (_, row) => (
              <tr key={row}>
                <td className="w-9 h-9 text-right pr-1.5 text-[11px] font-semibold text-ink-500">{row}</td>
                {Array.from({ length: size }, (_, col) => {
                  const v = matrix[row]?.[col] ?? 0
                  const intensity = maxVal > 0 ? v / maxVal : 0
                  const pct = (v * 100)
                  return (
                    <td
                      key={col}
                      className={clsx(
                        'w-[52px] h-9 text-center text-[11px] font-mono border border-ink-100/80',
                        intensity > 0.6 ? 'text-white font-bold' : 'text-ink-600',
                      )}
                      style={{ backgroundColor: intensity > 0.01 ? `rgba(112, 72, 232, ${Math.max(intensity * 0.7, 0.04)})` : 'transparent' }}
                    >
                      {pct >= 0.05 ? pct.toFixed(1) : ''}
                    </td>
                  )
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="flex items-center gap-2 text-[10px] text-ink-400">
        <span>%</span>
        <div className="flex-1 h-1 rounded-full bg-gradient-to-r from-ink-100 via-brand-300 to-brand-600" />
        <span>макс</span>
      </div>
    </div>
  )
}

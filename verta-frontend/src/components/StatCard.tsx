interface StatCardProps {
  value: number
  label: string
  color: string
}

export function StatCard({ value, label, color }: StatCardProps) {
  return (
    <div className="card stat-card">
      <div className="stat-bar" style={{ background: color }} />
      <div className="stat-value">{value}</div>
      <div className="stat-label">{label}</div>
    </div>
  )
}

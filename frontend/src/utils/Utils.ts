export const formatTimestamp = (ts?: string) => {
  if (!ts) return "";
  const date = new Date(ts);
  return date
    .toLocaleString("cs-CZ", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
    })
    .replace(/\.\s/g, ".");
};

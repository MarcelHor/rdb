import { Modal, Button, Table } from "react-bootstrap";
import type { RainyCityDto, StableDayDto, TempDiffCityDto } from "../api/build";

export default function QueryResultModal({
  show,
  onHide,
  result,
}: {
  show: boolean;
  onHide: () => void;
  result: any;
}) {
  const renderResult = () => {
    if (!result) return <p>Žádná data</p>;

    if (Array.isArray(result) && result[0]?.maxRain1h !== undefined) {
      return (
        <Table striped bordered>
          <thead>
            <tr>
              <th>Město</th>
              <th>Šířka</th>
              <th>Délka</th>
              <th>Déšť (1h)</th>
            </tr>
          </thead>
          <tbody>
            {result.map((item: RainyCityDto, idx: number) => (
              <tr key={idx}>
                <td>{item.name}</td>
                <td>{item.lat}</td>
                <td>{item.lon}</td>
                <td>{item.maxRain1h} mm</td>
              </tr>
            ))}
          </tbody>
        </Table>
      );
    }

    if (Array.isArray(result) && result[0]?.clouds !== undefined) {
      return (
        <Table striped bordered>
          <thead>
            <tr>
              <th>Město</th>
              <th>Datum</th>
              <th>Typ počasí</th>
              <th>Oblačnost</th>
              <th>Počet měření</th>
            </tr>
          </thead>
          <tbody>
            {result.map((item: StableDayDto, idx: number) => (
              <tr key={idx}>
                <td>{item.cityName}</td>
                <td>{item.date}</td>
                <td>{item.weatherType}</td>
                <td>{item.clouds} %</td>
                <td>{item.cnt}</td>
              </tr>
            ))}
          </tbody>
        </Table>
      );
    }

    if (result?.tempDiff !== undefined) {
      const item: TempDiffCityDto = result;
      return (
        <div>
          <p>
            <strong>Město:</strong> {item.name}
          </p>
          <p>
            <strong>Souřadnice:</strong> {item.lat}, {item.lon}
          </p>
          <p>
            <strong>Min. teplota:</strong> {item.tempMin} °C
          </p>
          <p>
            <strong>Max. teplota:</strong> {item.tempMax} °C
          </p>
          <p>
            <strong>Rozdíl:</strong> {item.tempDiff} °C
          </p>
        </div>
      );
    }

    if (typeof result === "object" && Object.keys(result).length === 0) {
      return <p>Data úspěšně vygenerována.</p>;
    }

    return <p>Neznámý formát odpovědi</p>;
  };

  return (
    <Modal show={show} onHide={onHide} centered size="lg">
      <Modal.Header closeButton>
        <Modal.Title>Výsledek dotazu</Modal.Title>
      </Modal.Header>
      <Modal.Body>{renderResult()}</Modal.Body>
      <Modal.Footer>
        <Button variant="secondary" onClick={onHide}>
          Zavřít
        </Button>
      </Modal.Footer>
    </Modal>
  );
}

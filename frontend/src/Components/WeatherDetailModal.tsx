import { Modal, Button, Image } from "react-bootstrap";
import type { WeatherRecordDto } from "../api/build";
import { formatTimestamp } from "../utils/Utils";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faClock,
  faCity,
  faTemperatureHalf,
  faDroplet,
  faGaugeHigh,
  faCloud,
  faWind,
  faInfoCircle,
} from "@fortawesome/free-solid-svg-icons";

export default function WeatherDetailModal({
  show,
  onHide,
  record,
}: {
  show: boolean;
  onHide: () => void;
  record: WeatherRecordDto | null;
}) {
  if (!record) return null;

  const icon = record.conditions?.[0]?.icon;
  const description = record.conditions?.[0]?.description;

  return (
    <Modal show={show} onHide={onHide} centered>
      <Modal.Header closeButton>
        <Modal.Title>
          {icon && (
            <Image
              src={`https://openweathermap.org/img/wn/${icon}@2x.png`}
              alt={description}
              style={{ width: 50, height: 50 }}
              className="ms-2"
            />
          )}
          Detail počasí
        </Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <p>
          <FontAwesomeIcon icon={faClock} className="me-2" />
          <strong>Datum:</strong> {formatTimestamp(record.timestamp)}
        </p>
        <p>
          <FontAwesomeIcon icon={faCity} className="me-2" />
          <strong>Město:</strong> {record.city?.name ?? "?"}
        </p>
        <p>
          <FontAwesomeIcon icon={faTemperatureHalf} className="me-2" />
          <strong>Teplota:</strong> {record.temp} °C
        </p>
        <p>
          <FontAwesomeIcon icon={faDroplet} className="me-2" />
          <strong>Vlhkost:</strong> {record.humidity} %
        </p>
        <p>
          <FontAwesomeIcon icon={faGaugeHigh} className="me-2" />
          <strong>Tlak:</strong> {record.pressure} hPa
        </p>
        <p>
          <FontAwesomeIcon icon={faCloud} className="me-2" />
          <strong>Oblačnost:</strong> {record.clouds} %
        </p>
        <p>
          <FontAwesomeIcon icon={faWind} className="me-2" />
          <strong>Vítr:</strong> {record.windSpeed} m/s
        </p>
        <p>
          <FontAwesomeIcon icon={faInfoCircle} className="me-2" />
          <strong>Popis:</strong> {description ?? "?"}
        </p>
      </Modal.Body>
      <Modal.Footer>
        <Button variant="secondary" onClick={onHide}>
          Zavřít
        </Button>
      </Modal.Footer>
    </Modal>
  );
}

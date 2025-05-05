import { useState } from "react";
import { Button, Dropdown, DropdownButton, Form, Table } from "react-bootstrap";
import type { LatLngExpression } from "leaflet";
import type { WeatherRecordDto } from "../api/build";
import WeatherDetailModal from "./WeatherDetailModal";
import { formatTimestamp } from "../utils/Utils";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faMagnifyingGlass, faInfoCircle } from "@fortawesome/free-solid-svg-icons";

export default function Sidebar({
  cityName,
  setCityName,
  daysBack,
  setDaysBack,
  records,
  loading,
  setMarker,
  handleCitySearch,
}: {
  cityName: string;
  setCityName: (name: string) => void;
  daysBack: number;
  setDaysBack: (days: number) => void;
  records: WeatherRecordDto[];
  setRecords: (records: WeatherRecordDto[]) => void;
  loading: boolean;
  setLoading: (loading: boolean) => void;
  marker: LatLngExpression | null;
  setMarker: (marker: LatLngExpression | null) => void;
  handleCitySearch: () => void;
}) {
  const [selectedRecord, setSelectedRecord] = useState<WeatherRecordDto | null>(
    null
  );
  const [modalOpen, setModalOpen] = useState(false);

  const openDetail = (rec: WeatherRecordDto) => {
    setSelectedRecord(rec);
    setModalOpen(true);
  };

  return (
    <>
      <div className="d-flex mb-3 gap-2">
        <Form
          onSubmit={(e) => {
            e.preventDefault();
            handleCitySearch();
          }}
        >
          <div className="d-flex mb-3 gap-2">
            <Form.Control
              type="text"
              placeholder="Město"
              value={cityName}
              onChange={(e) => {
                setCityName(e.target.value);
                setMarker(null);
              }}
            />
            <DropdownButton
              title={`${daysBack} dní`}
              onSelect={(val) => setDaysBack(Number(val))}
            >
              {[1, 2, 3, 4, 5, 6, 7].map((d) => (
                <Dropdown.Item key={d} eventKey={d}>
                  {d}
                </Dropdown.Item>
              ))}
            </DropdownButton>
            <Button
              variant="primary"
              type="submit"
              disabled={loading}
              onClick={handleCitySearch}
              className="d-flex align-items-center gap-2"
            >
              <FontAwesomeIcon icon={faMagnifyingGlass} />
              {loading ? "Načítám..." : "Hledat"}
            </Button>
          </div>
        </Form>
      </div>

      {records.length === 0 && !loading && (
        <div className="text-muted">Žádná data</div>
      )}
      {records.length > 0 && (
        <Table striped bordered size="sm">
          <thead>
            <tr>
              <th>Čas</th>
              <th>Město</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {records.map((rec, idx) => (
              <tr key={idx}>
                <td>{formatTimestamp(rec.timestamp)}</td>
                <td>{rec.city?.name ?? "?"}</td>
                <td className="d-flex justify-content-center align-items-center">
                  <Button
                    size="sm"
                    variant="outline-primary"
                    onClick={() => openDetail(rec)}
                    className="d-flex align-items-center gap-2"
                  >
                    <FontAwesomeIcon icon={faInfoCircle} />
                    Detail
                  </Button>
                </td>
              </tr>
            ))}
          </tbody>
        </Table>
      )}

      <WeatherDetailModal
        show={modalOpen}
        onHide={() => setModalOpen(false)}
        record={selectedRecord}
      />
    </>
  );
}

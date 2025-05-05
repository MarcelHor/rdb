import { Button, Dropdown, DropdownButton, Form } from "react-bootstrap";
import type { LatLngExpression } from "leaflet";
import type { WeatherRecordDto } from "../api/build";

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
            <Button variant="primary" type="submit">
              Hledat
            </Button>
          </div>
        </Form>
      </div>

      {/* Výpis počasí */}
      <div>
        {records.length === 0 && !loading && (
          <div className="text-muted">Žádná data</div>
        )}
        {records.map((rec, idx) => (
          <div key={idx} className="mb-2">
            <strong>{rec.timestamp}</strong>
            <br />
            Město: {rec.city?.name ?? "?"}
            <br />
            Teplota: {rec.temp} °C
          </div>
        ))}
      </div>
    </>
  );
}

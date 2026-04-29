import { Client } from '@stomp/stompjs';
import { useEffect } from 'react';
import { AnyRecord, WS_URL } from './api';

export function useLiveRfq(rfqId: string | undefined, onQuote: (event: AnyRecord) => void, onStatus: (event: AnyRecord) => void) {
  useEffect(() => {
    if (!rfqId) return;
    const client = new Client({
      brokerURL: WS_URL,
      reconnectDelay: 1200,
      debug: () => undefined,
      onConnect: () => {
        client.subscribe(`/topic/rfqs/${rfqId}/quotes`, message => onQuote(JSON.parse(message.body)));
        client.subscribe(`/topic/rfqs/${rfqId}/status`, message => onStatus(JSON.parse(message.body)));
      }
    });
    client.activate();
    return () => {
      void client.deactivate();
    };
  }, [rfqId, onQuote, onStatus]);
}

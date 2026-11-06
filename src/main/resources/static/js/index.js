// Eventos adicionados ao carregar a pagina
document.addEventListener('DOMContentLoaded', () => {

  'use strict'

  // Obter ID da URL
  const id = new URLSearchParams(window.location.search).get("id");
  const munId = id && /^\d+$/.test(id) ? id : "3524006";
  
  // Placeholder visual
  const popChartHtml = document.getElementById('popChart');
  const pibChartHtml = document.getElementById('pibChart');

  // Labels and Colors default
  const xValues = ["2001","2002","2003","2004","2005","2006","2007","2008","2009","2010"];

  // inicializa com zeros (evita layout shift)
  const initialData = [0,0,0,0,0,0,0,0,0,0];

  const popChart = new Chart(popChartHtml, {
    type: 'bar',
    data: { labels: xValues,
		    datasets: [{
				 data: initialData,
				 backgroundColor: 'rgba(54, 162, 235, 0.7)',
			     borderColor: 'rgba(54, 162, 235, 1)',
			     borderWidth: 1
		 	}]
	},
    options: {
      plugins: {
        legend: { display: false },
        tooltip: { boxPadding: 3 }
      }
    }
  });

  const pibChart = new Chart(pibChartHtml, {
    type: 'bar',
    data: { labels: xValues,
	        datasets: [{
				 data: initialData,
				 backgroundColor: 'rgba(40, 167, 69, 0.7)',
			     borderColor: 'rgba(40, 167, 69, 1)',
			     borderWidth: 1
		 	}]
	},
    options: {
      plugins: {
        legend: { display: false },
        tooltip: {
	        boxPadding: 3,
		    callbacks: {
		    label: function(context) {
		               return 'R$ ' +
			                  context.raw.toLocaleString('pt-BR', { minimumFractionDigits: 1, maximumFractionDigits: 1}) +
			                  ' milhões';
			       }
		    }
        }
      }
    }
  });
  
  getPopulacaoAll(munId, popChart);
  getPibAll(munId, pibChart);
  
});

// Chamada API SIGRA IBGE - Série População do Município
async function getPopulacaoAll(munId, chart) {
  try {
    const response = await fetch(`https://apisidra.ibge.gov.br/values/t/6579/n6/${munId}/v/9324/p/all`, {method: "GET"});
    if (!response.ok) {
      throw new Error("API SIDRA IBGE requisição status: " + response.status);
    }
    const dados = await response.json();
    console.log(dados);
	
	const populacaoPorAno = dados.slice(1).map(item => ({
	    ano: item.D3N,
	    populacao: Number(item.V)
	}));
	console.log(populacaoPorAno);

	chart.data.labels = dados.slice(1).map(item => item.D3N);
	chart.data.datasets[0].data = dados.slice(1).map(item => Number(item.V));	
	chart.update();
  } catch (error) {
    console.error("Erro ao obter série população:", error);
    return null;
  }
}

// Chamada API SIGRA IBGE - Série PIB do Município
async function getPibAll(munId, chart) {
  try {
    const response = await fetch(`https://apisidra.ibge.gov.br/values/t/5938/n6/${munId}/v/37/p/all`, {method: "GET"});
    if (!response.ok) {
      throw new Error("API SIDRA IBGE requisição status: " + response.status);
    }
    const dados = await response.json();
    console.log(dados);
	
	const pibPorAno = dados.slice(1).map(item => ({
	    ano: item.D3N,
	    pib: Number(item.V) / 1000
	}));
	console.log(pibPorAno);

	chart.data.labels = dados.slice(1).map(item => item.D3N);
	chart.data.datasets[0].data = dados.slice(1).map(item => Number(item.V) / 1000);
	chart.update();
  } catch (error) {
    console.error("Erro ao obter série pib:", error);
    return null;
  }
}
